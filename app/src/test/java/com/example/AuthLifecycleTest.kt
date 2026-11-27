package com.example

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Looper
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
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseBootstrap
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.NearGridRepository
import com.example.ui.AuthViewModel
import com.example.ui.FeatureStartupState
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
import kotlinx.coroutines.delay
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
import org.robolectric.Shadows.shadowOf
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
        assertNotNull("Configured Web Client ID must resolve from R.string / BuildConfig", resolved)
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
        assertEquals(1, fakeCredentialClient.capturedRequests.first().credentialOptions.size)
        assertTrue(
            fakeCredentialClient.capturedRequests.first().credentialOptions.first() is GetSignInWithGoogleOption
        )
    }

    @Test
    fun googleAuthRepository_attemptAutoSignInUsesAuthorizedGoogleIdOptionOnly() = runBlocking {
        val googleCred = GoogleIdTokenCredential(
            id = "returning@example.com",
            idToken = "silent.jwt.token",
            displayName = "Returning User",
            familyName = null,
            givenName = null,
            profilePictureUri = null,
            phoneNumber = null
        )
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(listOf(Result.success(GetCredentialResponse(googleCred))))
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.attemptAutoSignIn(app)
        assertTrue(outcome is AuthSignInOutcome.Success)
        assertEquals("silent.jwt.token", fakeAuthClient.lastIdToken)
        assertEquals(1, fakeCredentialClient.capturedRequests.size)
        val option = fakeCredentialClient.capturedRequests.first().credentialOptions.single()
        assertTrue(option is GetGoogleIdOption)
        assertTrue((option as GetGoogleIdOption).filterByAuthorizedAccounts)
        assertTrue(option.autoSelectEnabled)
    }

    @Test
    fun googleAuthRepository_attemptAutoSignInReturnsCancelledWhenNoAuthorizedAccountCached() = runBlocking {
        val fakeCredentialClient = FakeCredentialClient(
            responses = ArrayDeque(listOf(Result.failure(NoCredentialException("No authorized credential"))))
        )
        val fakeAuthClient = FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        val repo = GoogleAuthRepository(app, fakeCredentialClient, fakeAuthClient)

        val outcome = repo.attemptAutoSignIn(app)
        assertEquals(AuthSignInOutcome.Cancelled, outcome)
        assertEquals(1, fakeCredentialClient.capturedRequests.size)
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
    fun authViewModel_and_nearGridViewModel_handleUnauthenticatedAndStartupRecoveryCleanly() = runBlocking {
        FirebaseBootstrap.getAuth(app)?.signOut()
        val authVm = AuthViewModel(app)
        assertEquals(AuthState.Initializing, authVm.authState.value)

        // When no valid Activity is available, silent auto-sign-in falls back cleanly to Unauthenticated
        authVm.attemptStartupAutoSignIn(null)
        assertEquals(AuthState.Unauthenticated, authVm.authState.value)

        authVm.clearAuthError()
        authVm.signOut(app)
        shadowOf(Looper.getMainLooper()).idle()
        assertNull(authVm.currentUser.value)
        assertEquals(AuthState.Unauthenticated, authVm.authState.value)

        val viewModel = NearGridViewModel(app)
        repeat(300) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.startupState.value !is FeatureStartupState.Initializing) return@repeat
            delay(50)
        }
        assertEquals(FeatureStartupState.Ready, viewModel.startupState.value)
        assertNull(viewModel.currentUser.value)

        viewModel.prepareForSignOut()
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun nearGridViewModel_startupFailureEntersErrorStateAndRecoversOnRetry() = runBlocking {
        var shouldFail = true
        val viewModel = NearGridViewModel(app)
        repeat(300) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.startupState.value !is FeatureStartupState.Initializing) return@repeat
            delay(50)
        }

        viewModel.startupDependencyResolver = { context ->
            if (shouldFail) {
                throw IllegalStateException("Simulated cloud database outage")
            }
            val firestore = FirebaseBootstrap.getFirestore(context)!!
            val localRepo = NearGridRepository(AppDatabase.getDatabase(context))
            Pair(FirestoreRepository(firestore), localRepo)
        }

        // Trigger controlled failure
        viewModel.retryStartup()
        repeat(300) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.startupState.value is FeatureStartupState.Error) return@repeat
            delay(50)
        }
        val errorState = viewModel.startupState.value
        assertTrue("Expected Error state, got $errorState", errorState is FeatureStartupState.Error)
        assertTrue((errorState as FeatureStartupState.Error).message.contains("Simulated cloud database outage"))

        // Recover on retry
        shouldFail = false
        viewModel.retryStartup()
        repeat(300) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.startupState.value is FeatureStartupState.Ready) return@repeat
            delay(50)
        }
        assertEquals(FeatureStartupState.Ready, viewModel.startupState.value)
    }

    @Test
    fun firestoreRepository_sanitizeUserProfilePayloadExcludesPiiAndEnforcesRuleBounds() {
        val longString = "x".repeat(1200)
        val profile = UserProfile(
            userId = "uid_1",
            email = "private@example.com",
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
        assertFalse("Public profile payload must not leak PII email", payload.containsKey("email"))
        assertTrue((payload["handle"] as String).length <= 60)
        assertTrue((payload["bio"] as String).length <= 500)
        assertTrue((payload["avatarUri"] as String).length <= 1000)
        assertEquals("EXACT", payload["locationPrecision"])
        assertEquals(200, (payload["blockedUserIds"] as List<*>).size)
        assertTrue(payload.containsKey("createdAt"))
    }

    @Test
    fun firestoreRepository_sanitizePrivateAccountPayloadEnforcesRuleBounds() {
        val longEmail = "a".repeat(250) + "@example.com"
        val createPayload = FirestoreRepository.sanitizePrivateAccountPayload(
            uid = "uid_1",
            email = "  $longEmail  ",
            includeCreatedAt = true
        )
        assertEquals("uid_1", createPayload["userId"])
        assertTrue((createPayload["email"] as String).length <= 200)
        assertTrue(createPayload.containsKey("createdAt"))
        assertTrue(createPayload.containsKey("updatedAt"))

        val updatePayload = FirestoreRepository.sanitizePrivateAccountPayload(
            uid = "uid_1",
            email = "resident@example.com",
            includeCreatedAt = false
        )
        assertFalse(updatePayload.containsKey("createdAt"))
        assertEquals("resident@example.com", updatePayload["email"])
    }

    @Test
    fun googleAuthRepository_sessionValidationHandlesValidOfflineAndExpiredCorrectly() = runBlocking {
        val repo = GoogleAuthRepository(
            app,
            FakeCredentialClient(ArrayDeque()),
            FakeFirebaseAuthClient(auth = FirebaseBootstrap.getAuth(app))
        )

        assertEquals(
            SessionValidationResult.VALID,
            repo.classifySessionReload { /* valid */ }
        )
        assertEquals(
            SessionValidationResult.OFFLINE_OR_TRANSIENT,
            repo.classifySessionReload { throw FirebaseNetworkException("Offline") }
        )
        assertEquals(
            SessionValidationResult.EXPIRED_OR_INVALID,
            repo.classifySessionReload { throw FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "Disabled") }
        )
        assertEquals(
            SessionValidationResult.EXPIRED_OR_INVALID,
            repo.classifySessionReload { throw FirebaseAuthInvalidCredentialsException("ERROR_INVALID_USER_TOKEN", "Revoked") }
        )

        var cancellationPropagated = false
        try {
            repo.classifySessionReload { throw CancellationException("Cancelled") }
        } catch (_: CancellationException) {
            cancellationPropagated = true
        }
        assertTrue("CancellationException must propagate during session reload", cancellationPropagated)

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
    }

    private class FakeCredentialClient(
        private val responses: ArrayDeque<Result<GetCredentialResponse>>,
        private val clearError: Throwable? = null
    ) : GoogleCredentialClient {
        val capturedRequests = mutableListOf<GetCredentialRequest>()

        override suspend fun getCredential(
            context: Context,
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
