package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthSignInOutcome
import com.example.data.auth.AuthState
import com.example.data.auth.AuthenticatedUser
import com.example.data.auth.GoogleAuthRepository
import com.example.data.auth.SessionValidationResult
import com.example.data.remote.FirebaseBootstrap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight ViewModel dedicated strictly to authentication gating and session lifecycle.
 *
 * Scoped at the root navigation gate so feature ViewModels and Firestore snapshot listeners
 * are never instantiated on cold start before a verified Google session exists.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            Log.e(TAG, "Unhandled coroutine exception in AuthViewModel", throwable)
        }
    }

    private val auth: FirebaseAuth? by lazy {
        FirebaseBootstrap.getAuth(application)
    }

    private val googleAuthRepository: GoogleAuthRepository by lazy {
        GoogleAuthRepository(application)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authActionInFlight = AtomicBoolean(false)
    private val startupAutoSignInAttempted = AtomicBoolean(false)
    private var startupAutoSignInJob: Job? = null
    private var signInJob: Job? = null
    private var signOutJob: Job? = null
    private var verifyJob: Job? = null

    private var lastVerifiedUid: String? = null
    private var lastVerifiedAtMs: Long = 0L

    @Volatile
    private var explicitSignOutInSession: Boolean = false

    private val authStateListener = FirebaseAuth.AuthStateListener { fbAuth ->
        handleFirebaseUserChanged(fbAuth.currentUser)
    }

    init {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _authState.value = AuthState.Error(
                "Firebase Authentication could not be initialized. Please verify configuration."
            )
        } else {
            try {
                firebaseAuth.addAuthStateListener(authStateListener)
                val initialUser = runCatching { firebaseAuth.currentUser }.getOrNull()
                if (initialUser != null) {
                    _currentUser.value = initialUser
                    _authState.value = AuthState.Authenticated(AuthenticatedUser.fromFirebaseUser(initialUser))
                    verifySessionOnForeground(force = true)
                } else {
                    _authState.value = AuthState.Initializing
                }
            } catch (e: Exception) {
                Log.w(TAG, "AuthViewModel initialization failed", e)
                _authState.value = AuthState.Error(
                    "Unable to initialize authentication listener. Please restart the app."
                )
            }
        }
    }

    /**
     * Triggers silent startup auto-sign-in using an active [Activity] context once per cold start.
     * If no active [Activity] is available or the user explicitly signed out, falls back cleanly
     * to [AuthState.Unauthenticated] so the interactive Sign-In screen can be rendered.
     */
    fun attemptStartupAutoSignIn(activity: Activity?) {
        if (_authState.value !is AuthState.Initializing) return

        val existingUser = runCatching { auth?.currentUser }.getOrNull()
        if (existingUser != null) {
            _currentUser.value = existingUser
            _authState.value = AuthState.Authenticated(AuthenticatedUser.fromFirebaseUser(existingUser))
            verifySessionOnForeground(force = true)
            return
        }

        if (!startupAutoSignInAttempted.compareAndSet(false, true)) {
            return
        }

        if (explicitSignOutInSession || authActionInFlight.get()) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        startupAutoSignInJob?.cancel()
        startupAutoSignInJob = viewModelScope.launch(exceptionHandler) {
            try {
                when (val outcome = googleAuthRepository.attemptAutoSignIn(activity)) {
                    is AuthSignInOutcome.Success -> {
                        val fbUser = outcome.session.firebaseUser ?: auth?.currentUser
                        if (fbUser != null) {
                            _currentUser.value = fbUser
                            lastVerifiedUid = fbUser.uid
                            lastVerifiedAtMs = System.currentTimeMillis()
                            _authState.value = AuthState.Authenticated(
                                AuthenticatedUser.fromFirebaseUser(fbUser)
                            )
                        } else {
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                    is AuthSignInOutcome.Cancelled,
                    is AuthSignInOutcome.Error -> {
                        if (_authState.value is AuthState.Initializing) {
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                }
            } catch (e: CancellationException) {
                if (_authState.value is AuthState.Initializing) {
                    _authState.value = AuthState.Unauthenticated
                }
                throw e
            } catch (t: Throwable) {
                Log.w(TAG, "Silent startup auto-sign-in skipped: ${t.message}")
                if (_authState.value is AuthState.Initializing) {
                    _authState.value = AuthState.Unauthenticated
                }
            } finally {
                if (_authState.value is AuthState.Initializing) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    private fun handleFirebaseUserChanged(user: FirebaseUser?) {
        _currentUser.value = user
        if (user != null) {
            explicitSignOutInSession = false
            val currentAuth = _authState.value
            if (currentAuth !is AuthState.Authenticating) {
                if (currentAuth !is AuthState.Authenticated || currentAuth.uid != user.uid) {
                    _authState.value = AuthState.Authenticated(AuthenticatedUser.fromFirebaseUser(user))
                }
            }
        } else {
            lastVerifiedUid = null
            lastVerifiedAtMs = 0L
            val currentAuth = _authState.value
            if (currentAuth !is AuthState.Authenticating &&
                currentAuth !is AuthState.Initializing &&
                currentAuth !is AuthState.Error
            ) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        if (!authActionInFlight.compareAndSet(false, true)) {
            return
        }
        if (_authState.value is AuthState.Authenticating || _authState.value is AuthState.Authenticated) {
            authActionInFlight.set(false)
            return
        }
        startupAutoSignInJob?.cancel()
        explicitSignOutInSession = false
        _authState.value = AuthState.Authenticating
        signInJob = viewModelScope.launch(exceptionHandler) {
            try {
                when (val outcome = googleAuthRepository.signInWithGoogle(activity)) {
                    is AuthSignInOutcome.Success -> {
                        val user = outcome.session.firebaseUser ?: auth?.currentUser
                        if (user == null) {
                            _authState.value = AuthState.Error(
                                "Firebase user session was unavailable after sign-in."
                            )
                            return@launch
                        }
                        _currentUser.value = user
                        lastVerifiedUid = user.uid
                        lastVerifiedAtMs = System.currentTimeMillis()
                        _authState.value = AuthState.Authenticated(
                            AuthenticatedUser.fromFirebaseUser(user)
                        )
                    }
                    is AuthSignInOutcome.Cancelled -> {
                        _authState.value = AuthState.Unauthenticated
                    }
                    is AuthSignInOutcome.Error -> {
                        _authState.value = AuthState.Error(outcome.message)
                    }
                }
            } catch (e: CancellationException) {
                if (_authState.value is AuthState.Authenticating) {
                    _authState.value = AuthState.Unauthenticated
                }
                throw e
            } catch (t: Throwable) {
                Log.e(TAG, "Unexpected Google Sign-In failure", t)
                _authState.value = AuthState.Error(GoogleAuthRepository.mapAuthErrorToMessage(t))
            } finally {
                authActionInFlight.set(false)
            }
        }
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun verifySessionOnForeground(force: Boolean = false) {
        val currentAuth = _authState.value as? AuthState.Authenticated ?: return
        val user = currentAuth.user ?: return
        if (signOutJob?.isActive == true || authActionInFlight.get() || verifyJob?.isActive == true) {
            return
        }
        val now = System.currentTimeMillis()
        if (!force && lastVerifiedUid == user.uid && (now - lastVerifiedAtMs) < SESSION_VERIFY_INTERVAL_MS) {
            return
        }
        verifyJob = viewModelScope.launch(exceptionHandler) {
            when (googleAuthRepository.validateSession(user)) {
                SessionValidationResult.VALID -> {
                    lastVerifiedUid = user.uid
                    lastVerifiedAtMs = System.currentTimeMillis()
                }
                SessionValidationResult.OFFLINE_OR_TRANSIENT -> {
                    // Preserve cached session while offline or during transient errors
                }
                SessionValidationResult.EXPIRED_OR_INVALID -> {
                    withContext(NonCancellable) {
                        explicitSignOutInSession = true
                        googleAuthRepository.clearCredentialAndSignOut(getApplication())
                        lastVerifiedUid = null
                        lastVerifiedAtMs = 0L
                        _currentUser.value = null
                        _authState.value = AuthState.Error(
                            "Your session has expired or been revoked. Please sign in again with Google."
                        )
                    }
                }
            }
        }
    }

    fun signOut(
        context: Context,
        onBeforeSignOut: (suspend () -> Unit)? = null
    ) {
        if (signOutJob?.isActive == true) return
        signOutJob = viewModelScope.launch(exceptionHandler) {
            withContext(NonCancellable) {
                explicitSignOutInSession = true
                startupAutoSignInJob?.cancel()
                signInJob?.cancel()
                verifyJob?.cancel()
                authActionInFlight.set(false)
                try {
                    onBeforeSignOut?.invoke()
                } catch (e: Exception) {
                    Log.d(TAG, "Pre-sign-out cleanup skipped: ${e.message}")
                }
                googleAuthRepository.clearCredentialAndSignOut(context)
                lastVerifiedUid = null
                lastVerifiedAtMs = 0L
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { auth?.removeAuthStateListener(authStateListener) }
        startupAutoSignInJob?.cancel()
        signInJob?.cancel()
        signOutJob?.cancel()
        verifyJob?.cancel()
    }

    companion object {
        private const val TAG = "AuthViewModel"
        private const val SESSION_VERIFY_INTERVAL_MS = 60_000L
    }
}
