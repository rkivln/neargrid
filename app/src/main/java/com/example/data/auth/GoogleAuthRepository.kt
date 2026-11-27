package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.remote.FirebaseBootstrap
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Abstracts Credential Manager calls for reliable error handling and unit testing.
 */
interface GoogleCredentialClient {
    suspend fun getCredential(context: Context, request: GetCredentialRequest): GetCredentialResponse
    suspend fun clearCredentialState(context: Context)
}

internal class AndroidGoogleCredentialClient : GoogleCredentialClient {
    override suspend fun getCredential(
        context: Context,
        request: GetCredentialRequest
    ): GetCredentialResponse {
        val credentialManager = CredentialManager.create(context)
        return credentialManager.getCredential(
            context = context,
            request = request
        )
    }

    override suspend fun clearCredentialState(context: Context) {
        val credentialManager = CredentialManager.create(context.applicationContext ?: context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

/**
 * Abstracts Firebase Auth operations for reliable lifecycle handling and unit testing.
 */
interface FirebaseAuthClient {
    fun getAuthOrNull(): FirebaseAuth?
    suspend fun signInWithGoogleIdToken(idToken: String): AuthenticatedUser
    suspend fun reloadUser(user: FirebaseUser)
    fun signOut()
}

internal class AndroidFirebaseAuthClient(private val context: Context) : FirebaseAuthClient {
    private val appContext = context.applicationContext ?: context

    override fun getAuthOrNull(): FirebaseAuth? = FirebaseBootstrap.getAuth(appContext)

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthenticatedUser {
        val auth = getAuthOrNull()
            ?: throw IllegalStateException("Firebase Authentication is not configured.")
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(authCredential).await()
        val firebaseUser = result.user
            ?: throw IllegalStateException("Firebase sign-in completed without a user session.")
        return AuthenticatedUser.fromFirebaseUser(firebaseUser)
    }

    override suspend fun reloadUser(user: FirebaseUser) {
        user.reload().await()
    }

    override fun signOut() {
        getAuthOrNull()?.signOut()
    }
}

/**
 * Orchestrates Google Credential Manager + Firebase GoogleAuthProvider authentication.
 */
class GoogleAuthRepository(
    context: Context,
    private val credentialClient: GoogleCredentialClient = AndroidGoogleCredentialClient(),
    private val firebaseAuthClient: FirebaseAuthClient = AndroidFirebaseAuthClient(context),
    private val webClientIdProvider: (Context) -> String? = { FirebaseBootstrap.resolveWebClientId(it) }
) {
    private val appContext = context.applicationContext ?: context

    fun getAuthOrNull(): FirebaseAuth? = firebaseAuthClient.getAuthOrNull()

    /**
     * Silent startup auto-sign-in for returning users who previously authorized Google Sign-In.
     * Uses a single-option [GetGoogleIdOption] request with `filterByAuthorizedAccounts = true`
     * and `autoSelectEnabled = true`, falling back silently to [AuthSignInOutcome.Cancelled] so
     * the interactive Sign-In screen can be shown whenever no authorized credential is available.
     */
    suspend fun attemptAutoSignIn(context: Context = appContext): AuthSignInOutcome {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            Log.d(TAG, "Skipping silent auto-sign-in: Activity is finishing or destroyed")
            return AuthSignInOutcome.Cancelled
        }

        val existingUser = runCatching { firebaseAuthClient.getAuthOrNull()?.currentUser }.getOrNull()
        if (existingUser != null) {
            return AuthSignInOutcome.Success(AuthenticatedUser.fromFirebaseUser(existingUser))
        }

        val serverClientId = webClientIdProvider(appContext)
        if (!FirebaseBootstrap.isValidWebClientId(serverClientId)) {
            Log.w(TAG, "Skipping silent auto-sign-in: Web Client ID unavailable")
            return AuthSignInOutcome.Cancelled
        }
        val validatedClientId = serverClientId!!

        if (firebaseAuthClient.getAuthOrNull() == null) {
            return AuthSignInOutcome.Cancelled
        }

        return try {
            val request = buildSilentAutoSignInRequest(validatedClientId)
            val response = credentialClient.getCredential(context, request)
            val idToken = extractGoogleIdToken(response)
            val session = firebaseAuthClient.signInWithGoogleIdToken(idToken)
            AuthSignInOutcome.Success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Silent Google auto-sign-in cancelled or dismissed: ${e.message}", e)
            AuthSignInOutcome.Cancelled
        } catch (e: NoCredentialException) {
            Log.d(TAG, "No authorized Google credential available for silent auto-sign-in")
            AuthSignInOutcome.Cancelled
        } catch (t: Throwable) {
            Log.d(TAG, "Silent auto-sign-in skipped (${t.javaClass.simpleName}): ${t.message}")
            AuthSignInOutcome.Cancelled
        }
    }

    suspend fun signInWithGoogle(activity: Activity): AuthSignInOutcome {
        if (activity.isFinishing || activity.isDestroyed) {
            return AuthSignInOutcome.Error("Sign-in window is no longer active. Please try again.")
        }

        val serverClientId = webClientIdProvider(appContext)
        if (!FirebaseBootstrap.isValidWebClientId(serverClientId)) {
            Log.e(TAG, "Missing or invalid Google OAuth Web Client ID configuration")
            return AuthSignInOutcome.Error(
                "Google Sign-In is not configured. Missing a valid OAuth 2.0 Web Client ID in Firebase configuration."
            )
        }
        val validatedClientId = serverClientId!!

        if (firebaseAuthClient.getAuthOrNull() == null) {
            return AuthSignInOutcome.Error(
                "Firebase Authentication could not be initialized. Please check your Firebase configuration."
            )
        }

        return try {
            val response = requestGoogleCredential(activity, validatedClientId)
            val idToken = extractGoogleIdToken(response)
            val session = firebaseAuthClient.signInWithGoogleIdToken(idToken)
            AuthSignInOutcome.Success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            AuthSignInOutcome.Cancelled
        } catch (e: Throwable) {
            Log.e(TAG, "Google Sign-In failed: ${e.javaClass.simpleName}", e)
            AuthSignInOutcome.Error(mapAuthErrorToMessage(e))
        }
    }

    /**
     * Attempts explicit Sign-In with Google first, and falls back once to [GetGoogleIdOption]
     * if the device/provider fails with a provider/no-credential error (never after cancellation).
     */
    private suspend fun requestGoogleCredential(
        activity: Activity,
        serverClientId: String
    ): GetCredentialResponse {
        val signInButtonRequest = buildSignInWithGoogleRequest(serverClientId)
        return try {
            credentialClient.getCredential(activity, signInButtonRequest)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            throw e
        } catch (e: Exception) {
            val shouldTryGoogleIdFallback = e is NoCredentialException ||
                e is GetCredentialUnsupportedException ||
                e is GetCredentialProviderConfigurationException
            if (!shouldTryGoogleIdFallback) {
                throw e
            }
            Log.w(TAG, "Primary Sign-In request failed (${e.javaClass.simpleName}); attempting GetGoogleIdOption fallback")
            val googleIdRequest = buildGoogleIdRequest(serverClientId)
            credentialClient.getCredential(activity, googleIdRequest)
        }
    }

    internal fun buildSilentAutoSignInRequest(serverClientId: String): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    internal fun buildSignInWithGoogleRequest(serverClientId: String): GetCredentialRequest {
        val option = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    internal fun buildGoogleIdRequest(serverClientId: String): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    internal fun extractGoogleIdToken(response: GetCredentialResponse): String {
        val credential = response.credential
        val isGoogleIdCredential = credential is CustomCredential && (
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
            )
        if (!isGoogleIdCredential) {
            throw IllegalStateException("Unexpected credential type returned by Google Sign-In.")
        }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken.trim()
        if (idToken.isEmpty()) {
            throw IllegalStateException("Google Sign-In returned an empty ID token.")
        }
        return idToken
    }

    suspend fun validateSession(user: FirebaseUser): SessionValidationResult =
        classifySessionReload { firebaseAuthClient.reloadUser(user) }

    internal suspend fun classifySessionReload(
        reloadBlock: suspend () -> Unit
    ): SessionValidationResult {
        return try {
            reloadBlock()
            SessionValidationResult.VALID
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.w(TAG, "Persisted Firebase session is invalid or disabled: ${e.errorCode}")
            SessionValidationResult.EXPIRED_OR_INVALID
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "Persisted Firebase credentials expired or revoked: ${e.errorCode}")
            SessionValidationResult.EXPIRED_OR_INVALID
        } catch (e: FirebaseNetworkException) {
            Log.d(TAG, "Offline while validating session; preserving persisted user")
            SessionValidationResult.OFFLINE_OR_TRANSIENT
        } catch (e: Exception) {
            Log.w(TAG, "Transient error validating session; preserving persisted user", e)
            SessionValidationResult.OFFLINE_OR_TRANSIENT
        }
    }

    suspend fun clearCredentialAndSignOut(context: Context) {
        withContext(NonCancellable) {
            try {
                credentialClient.clearCredentialState(context)
            } catch (e: Throwable) {
                Log.w(TAG, "Credential Manager state clear skipped: ${e.message}")
            } finally {
                try {
                    firebaseAuthClient.signOut()
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseAuth signOut error: ${e.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "GoogleAuthRepository"

        fun mapAuthErrorToMessage(throwable: Throwable): String {
            return when (throwable) {
                is NoCredentialException ->
                    "No Google account found on this device. Add a Google account in device Settings and try again."

                is GetCredentialInterruptedException ->
                    "Google Sign-In was interrupted. Please try again."

                is GetCredentialUnsupportedException ->
                    "Google Sign-In is not supported on this device or Google Play Services needs an update."

                is GetCredentialProviderConfigurationException ->
                    "Google Sign-In service is unavailable. Ensure Google Play Services is enabled and updated."

                is GetCredentialCustomException ->
                    classifyGenericCredentialMessage(throwable.errorMessage?.toString() ?: throwable.message)

                is GetCredentialException ->
                    classifyGenericCredentialMessage(throwable.errorMessage?.toString() ?: throwable.message)

                is GoogleIdTokenParsingException ->
                    "Could not verify the Google ID token returned by this device. Please try again."

                is FirebaseNetworkException ->
                    "No internet connection. Connect to a network and try signing in again."

                is FirebaseTooManyRequestsException ->
                    "Too many sign-in attempts. Please wait a moment and try again."

                is FirebaseAuthInvalidCredentialsException ->
                    "Your Google sign-in credential expired or is invalid. Please try signing in again."

                is FirebaseAuthInvalidUserException ->
                    "This Google account has been disabled or removed from Firebase Authentication."

                is FirebaseAuthUserCollisionException ->
                    "An account already exists with different credentials."

                is FirebaseAuthException ->
                    classifyFirebaseAuthException(throwable)

                else -> {
                    val raw = throwable.message ?: ""
                    if (raw.contains("network", ignoreCase = true) ||
                        raw.contains("timeout", ignoreCase = true) ||
                        raw.contains("unable to resolve host", ignoreCase = true)
                    ) {
                        "Network error during sign-in. Check your internet connection and try again."
                    } else if (throwable.localizedMessage?.isNotBlank() == true &&
                        !raw.contains("YOUR_DEFAULT_WEB_CLIENT_ID")
                    ) {
                        throwable.localizedMessage!!
                    } else {
                        "Unable to complete Google Sign-In. Please try again."
                    }
                }
            }
        }

        private fun classifyGenericCredentialMessage(rawMessage: String?): String {
            val msg = rawMessage.orEmpty()
            return when {
                msg.contains("28444") ||
                    msg.contains("DEVELOPER_ERROR", ignoreCase = true) ||
                    msg.contains("10:") ||
                    msg.contains("Caller has been temporarily blocked", ignoreCase = true) ->
                    "Google Sign-In configuration mismatch. Verify the OAuth Web Client ID and app SHA-1 certificate in Firebase Console."

                msg.contains("16:") || msg.contains("reauth", ignoreCase = true) ->
                    "Google Sign-In session timed out or was cancelled. Please try again."

                msg.contains("7:") || msg.contains("network", ignoreCase = true) ||
                    msg.contains("connection", ignoreCase = true) ->
                    "Network connection unavailable for Google Sign-In. Please try again when online."

                msg.contains("17:") || msg.contains("API_NOT_CONNECTED", ignoreCase = true) ->
                    "Google Play Services is temporarily unavailable. Please update Play Services and try again."

                msg.isNotBlank() -> msg
                else -> "Unable to complete Google Sign-In. Please try again."
            }
        }

        private fun classifyFirebaseAuthException(exception: FirebaseAuthException): String {
            val code = exception.errorCode
            val msg = exception.message.orEmpty()
            return when {
                code == "ERROR_NETWORK_REQUEST_FAILED" ->
                    "Network error while contacting Firebase Authentication. Check your connection and try again."
                code == "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Google Sign-In provider is not enabled in Firebase Authentication Console."
                code == "ERROR_INVALID_CREDENTIAL" ->
                    "Invalid or expired Google OAuth credential. Verify the Web Client ID matches your Firebase project."
                msg.contains("API key", ignoreCase = true) ->
                    "Firebase API configuration error. Verify google-services.json matches this project."
                else ->
                    "Authentication failed ($code). Please try again."
            }
        }
    }
}
