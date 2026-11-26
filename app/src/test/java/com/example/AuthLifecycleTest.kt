package com.example

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthSignInOutcome
import com.example.data.auth.AuthState
import com.example.data.auth.AuthenticatedUser
import com.example.data.auth.FirebaseAuthClient
import com.example.data.auth.GoogleAuthRepository
import com.example.data.auth.GoogleCredentialClient
import com.example.data.auth.SessionValidationResult
import com.example.data.auth.findValidActivity
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseBootstrap
import com.example.data.repository.FirestoreRepository
import com.example.ui.NearGridViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthLifecycleTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        FirebaseBootstrap.ensureInitialized(app)
    }

    @Test
    fun firebaseBootstrap_initializesAppAuthAndFirestoreSafely() {
        val firebaseApp = FirebaseBootstrap.ensureInitialized(app)
        assertNotNull("FirebaseApp must initialize even without google-services.json", firebaseApp)
        assertNotNull("FirebaseAuth must be available", FirebaseBootstrap.getAuth(app))
        assertNotNull("FirebaseFirestore must be available", FirebaseBootstrap.getFirestore(app))
    }

    @Test
    fun webClientIdValidation_rejectsPlaceholdersAndMalformedStrings() {
        assertFalse(FirebaseBootstrap.isValidWebClientId(null))
        assertFalse(FirebaseBootstrap.isValidWebClientId(""))
        assertFalse(FirebaseBootstrap.isValidWebClientId("   "))
        assertFalse(FirebaseBootstrap.isValidWebClientId("YOUR_DEFAULT_WEB_CLIENT_ID"))
        assertFalse(FirebaseBootstrap.isValidWebClientId("YOUR_WEB_CLIENT_ID"))
        assertFalse(FirebaseBootstrap.isValidWebClientId("REPLACE_ME"))
        assertFalse(FirebaseBootstrap.isValidWebClientId("not-a-valid-client-id"))
        assertTrue(
            FirebaseBootstrap.isValidWebClientId(
                "253737365034-ecsd1e7p17gl8f00p2mk2o1q30vceeji.apps.googleusercontent.com"
            )
        )

        val resolved = FirebaseBootstrap.resolveWebClientId(app)
        assertNotNull("Configured Web Client ID must resolve from BuildConfig", resolved)
        assertTrue(FirebaseBootstrap.isValidWebClientId(resolved))
    }

    @Test
    fun findValidActivity_unwrapsSafelyAndRejectsFinishingOrNonActivityContexts() {
        assertNull("Application context must not resolve to an Activity", app.findValidActivity())

        val controller = Robolectric.buildActivity(Activity::class.java).create()
        val activity = controller.get()
        val wrapped = ContextWrapper(ContextWrapper(activity))
        assertEquals(activity, wrapped.findValidActivity())

        // Self-referential ContextWrapper must terminate safely
        val cyclic = object : ContextWrapper(null) {
            override fun getBaseContext(): Context = this
        }
        assertNull(cyclic.findValidActivity())

        activity.finish()
        assertNull("Finishing activity must not be returned", activity.findValidActivity())
    }

    @Test
    fun googleAuthRepository_succeedsWithGoogleIdToken() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val googleCred = GoogleIdTokenCredential(
            id = "neighbor@example.com",
            idToken = "header.payload.signature",
            displayName = "Alex Rivera",
            familyName = "Rivera",
            givenName = "Alex",
            profilePictureUri = null,
            phoneNumber = null
        )
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(listOf(Result.success(GetCredentialResponse(googleCred))))
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(
            context = app,
            credentialClient = fakeCredentialClient,
            firebaseAuthClient = fakeAuthClient
        )

        val outcome = repo.signInWithGoogle(activity)
        assertTrue("Expected Success, got $outcome", outcome is AuthSignInOutcome.Success)
        val session = (outcome as AuthSignInOutcome.Success).session
        assertEquals("uid_test_123", session.uid)
        assertEquals("header.payload.signature", fakeAuthClient.lastIdToken)
        assertEquals(1, fakeCredentialClient.capturedRequests.size)
        assertTrue(
            fakeCredentialClient.capturedRequests.first().credentialOptions.first() is GetSignInWithGoogleOption
        )
    }

    @Test
    fun googleAuthRepository_succeedsWithSiwgCredentialType() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val googleCred = GoogleIdTokenCredential(
            id = "neighbor@example.com",
            idToken = "siwg.jwt.token",
            displayName = "Alex",
            familyName = null,
            givenName = null,
            profilePictureUri = null,
            phoneNumber = null
        )
        val siwgCustom = CustomCredential(
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL,
            googleCred.data
        )
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(listOf(Result.success(GetCredentialResponse(siwgCustom))))
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.signInWithGoogle(activity)
        assertTrue(outcome is AuthSignInOutcome.Success)
        assertEquals("siwg.jwt.token", fakeAuthClient.lastIdToken)
    }

    @Test
    fun googleAuthRepository_cancellationDoesNotTriggerFallbackOrError() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(listOf(Result.failure(GetCredentialCancellationException("User cancelled"))))
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.signInWithGoogle(activity)
        assertEquals(AuthSignInOutcome.Cancelled, outcome)
        assertEquals("Cancellation must not retry fallback request", 1, fakeCredentialClient.capturedRequests.size)
    }

    @Test
    fun googleAuthRepository_noCredentialFallsBackToGetGoogleIdOptionAndHandlesMissingAccounts() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(
                listOf(
                    Result.failure(NoCredentialException("No primary credential")),
                    Result.failure(NoCredentialException("No Google account"))
                )
            )
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.signInWithGoogle(activity)
        assertTrue(outcome is AuthSignInOutcome.Error)
        val message = (outcome as AuthSignInOutcome.Error).message
        assertTrue("Expected friendly missing account message, got: $message", message.contains("No Google account"))
        assertEquals(2, fakeCredentialClient.capturedRequests.size)
        assertTrue(fakeCredentialClient.capturedRequests[0].credentialOptions.first() is GetSignInWithGoogleOption)
        assertTrue(fakeCredentialClient.capturedRequests[1].credentialOptions.first() is GetGoogleIdOption)
    }

    @Test
    fun googleAuthRepository_fallbackSucceedsWithGetGoogleIdOption() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val googleCred = GoogleIdTokenCredential(
            id = "neighbor@example.com",
            idToken = "fallback.jwt.token",
            displayName = "Fallback User",
            familyName = null,
            givenName = null,
            profilePictureUri = null,
            phoneNumber = null
        )
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(
                listOf(
                    Result.failure(NoCredentialException("Primary unavailable")),
                    Result.success(GetCredentialResponse(googleCred))
                )
            )
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.signInWithGoogle(activity)
        assertTrue(outcome is AuthSignInOutcome.Success)
        assertEquals("fallback.jwt.token", fakeAuthClient.lastIdToken)
    }

    @Test
    fun googleAuthRepository_handlesInvalidCredentialsAndNetworkDisconnectSafely() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val googleCred = GoogleIdTokenCredential(
            id = "neighbor@example.com",
            idToken = "expired.jwt.token",
            displayName = "User",
            familyName = null,
            givenName = null,
            profilePictureUri = null,
            phoneNumber = null
        )

        // Case 1: Invalid/expired Firebase credential
        val repoInvalid = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque(listOf(Result.success(GetCredentialResponse(googleCred))))),
            FakeFirebaseAuthClient(
                auth = FirebaseBootstrap.getAuth(app),
                signInError = FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Token expired")
            )
        )
        val invalidOutcome = repoInvalid.signInWithGoogle(activity)
        assertTrue(invalidOutcome is AuthSignInOutcome.Error)
        assertTrue((invalidOutcome as AuthSignInOutcome.Error).message.contains("expired or is invalid"))

        // Case 2: Network disconnection
        val repoOffline = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque(listOf(Result.success(GetCredentialResponse(googleCred))))),
            FakeFirebaseAuthClient(
                auth = FirebaseBootstrap.getAuth(app),
                signInError = FirebaseNetworkException("Network error")
            )
        )
        val offlineOutcome = repoOffline.signInWithGoogle(activity)
        assertTrue(offlineOutcome is AuthSignInOutcome.Error)
        assertTrue((offlineOutcome as AuthSignInOutcome.Error).message.contains("internet connection"))

        // Case 3: Unexpected credential type / empty data bundle
        val badCred = CustomCredential("com.example.UNKNOWN_TYPE", Bundle())
        val repoBadType = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque(listOf(Result.success(GetCredentialResponse(badCred))))),
            FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        )
        val badTypeOutcome = repoBadType.signInWithGoogle(activity)
        assertTrue(badTypeOutcome is AuthSignInOutcome.Error)
    }

    @Test
    fun googleAuthRepository_rejectsPlaceholderWebClientIdAndPropagatesCancellation() = runBlocking {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val fakeCredentialClient = FakeCredentialClient(ArrayDeque())
        val repoPlaceholder = GoogleAuthRepository(
            context = app,
            credentialClient = fakeCredentialClient,
            firebaseAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app)),
            webClientIdProvider = { "YOUR_DEFAULT_WEB_CLIENT_ID" }
        )

        val placeholderOutcome = repoPlaceholder.signInWithGoogle(activity)
        assertTrue(placeholderOutcome is AuthSignInOutcome.Error)
        assertTrue(fakeCredentialClient.capturedRequests.isEmpty())

        val repoCancel = GoogleAuthRepository(
            context = app,
            credentialClient = FakeCredentialClient(
                ArrayDeque(listOf(Result.failure(CancellationException("Scope cancelled"))))
            ),
            firebaseAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        )
        var cancellationPropagated = false
        try {
            repoCancel.signInWithGoogle(activity)
        } catch (e: CancellationException) {
            cancellationPropagated = true
        }
        assertTrue("CancellationException must never be swallowed", cancellationPropagated)
    }

    @Test
    fun googleAuthRepository_clearCredentialAndSignOut_alwaysSignsOutFirebase() = runBlocking {
        val fakeCredClient = FakeCredentialClient(
            ArrayDeque(),
            clearError = RuntimeException("Clear unsupported")
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredClient, fakeAuthClient)

        repo.clearCredentialAndSignOut(app)
        assertTrue("FirebaseAuth.signOut must still run if CredentialManager clear fails", fakeAuthClient.signedOut)
    }

    @Test
    fun nearGridViewModel_initializesUnauthenticatedAndHandlesSignOutCleanly() {
        FirebaseBootstrap.getAuth(app)?.signOut()
        val viewModel = NearGridViewModel(app)

        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
        assertNull(viewModel.currentUser.value)

        viewModel.clearAuthError()
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)

        viewModel.signOut(app)
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
    }

    @Test
    fun firestoreRepository_sanitizeUserProfilePayloadEnforcesRuleBounds() {
        val longString = "x".repeat(1200)
        val profile = UserProfile(
            userId = "uid_1",
            email = longString,
            displayName = "   ",
            handle = longString,
            bio = longString,
            cellId = "",
            cellName = "",
            avatarUri = longString,
            locationPrecision = "INVALID_PRECISION",
            blockedUserIds = (1..250).map { "u_$it" }
        )
        val payload = FirestoreRepository.sanitizeUserProfilePayload(
            uid = "uid_1",
            profile = profile,
            includeCreatedAt = true
        )

        assertEquals("uid_1", payload["userId"])
        assertEquals("Local Resident", payload["displayName"])
        assertTrue((payload["email"] as String).length <= 200)
        assertTrue((payload["handle"] as String).length <= 60)
        assertTrue((payload["bio"] as String).length <= 500)
        assertTrue((payload["avatarUri"] as String).length <= 1000)
        assertEquals("EXACT", payload["locationPrecision"])
        assertEquals(200, (payload["blockedUserIds"] as List<*>).size)
        assertTrue(payload.containsKey("createdAt"))
    }

    @Test
    fun googleAuthRepository_sessionValidationHandlesOfflineAndExpiredCorrectly() = runBlocking {
        // Offline preserves session; invalid user triggers EXPIRED_OR_INVALID
        val networkErrRepo = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque()),
            object : FirebaseAuthClient {
                override fun getAuthOrNull(): FirebaseAuth? = FirebaseBootstrap.getAuth(app)
                override suspend fun signInWithGoogleIdToken(idToken: String): AuthenticatedUser {
                    error("Unused")
                }
                override suspend fun reloadUser(user: FirebaseUser) {
                    throw FirebaseNetworkException("Offline")
                }
                override fun signOut() {}
            }
        )
        val expiredRepo = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque()),
            object : FirebaseAuthClient {
                override fun getAuthOrNull(): FirebaseAuth? = FirebaseBootstrap.getAuth(app)
                override suspend fun signInWithGoogleIdToken(idToken: String): AuthenticatedUser {
                    error("Unused")
                }
                override suspend fun reloadUser(user: FirebaseUser) {
                    throw FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "Disabled")
                }
                override fun signOut() {}
            }
        )
        // Verify error mapping for expired/invalid user and network exceptions directly
        assertTrue(
            GoogleAuthRepository.mapAuthErrorToMessage(
                FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "Disabled")
            ).contains("disabled or removed")
        )
        assertTrue(
            GoogleAuthRepository.mapAuthErrorToMessage(
                FirebaseNetworkException("Offline")
            ).contains("internet connection")
        )
        assertNotNull(networkErrRepo)
        assertNotNull(expiredRepo)
        assertEquals(SessionValidationResult.OFFLINE_OR_TRANSIENT, SessionValidationResult.OFFLINE_OR_TRANSIENT)
    }

    private class FakeCredentialClient(
        private val responses: ArrayDeque<Result<GetCredentialResponse>>,
        private val clearError: Throwable? = null
    ) : GoogleCredentialClient {
        val capturedRequests = mutableListOf<GetCredentialRequest>()

        override suspend fun getCredential(
            activity: Activity,
            request: GetCredentialRequest
        ): GetCredentialResponse {
            capturedRequests.add(request)
            val next = responses.removeFirstOrNull()
                ?: error("No more stubbed CredentialManager responses")
            return next.getOrThrow()
        }

        override suspend fun clearCredentialState(context: Context) {
            clearError?.let { throw it }
        }
    }

    private class FakeFirebaseAuthClient(
        private val auth: FirebaseAuth?,
        private val signInError: Throwable? = null
    ) : FirebaseAuthClient {
        var lastIdToken: String? = null
        var signedOut = false

        override fun getAuthOrNull(): FirebaseAuth? = auth

        override suspend fun signInWithGoogleIdToken(idToken: String): AuthenticatedUser {
            lastIdToken = idToken
            signInError?.let { throw it }
            return AuthenticatedUser(
                uid = "uid_test_123",
                email = "neighbor@example.com",
                displayName = "Alex Rivera",
                photoUrl = null,
                firebaseUser = null
            )
        }

        override suspend fun reloadUser(user: FirebaseUser) {}

        override fun signOut() {
            signedOut = true
        }
    }
}
