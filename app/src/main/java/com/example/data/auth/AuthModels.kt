package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.firebase.auth.FirebaseUser

/**
 * Lightweight, test-friendly representation of an authenticated Firebase user session.
 */
data class AuthenticatedUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val firebaseUser: FirebaseUser? = null
) {
    companion object {
        fun fromFirebaseUser(user: FirebaseUser): AuthenticatedUser = AuthenticatedUser(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl?.toString(),
            firebaseUser = user
        )
    }
}

/**
 * Reliable state machine for NearGrid authentication lifecycle.
 */
sealed interface AuthState {
    /** Initial startup check while Firebase restores any persisted session. */
    data object Initializing : AuthState

    /** No active Firebase session; login screen should be shown. */
    data object Unauthenticated : AuthState

    /** Google Credential Manager / Firebase credential exchange is in progress. */
    data object Authenticating : AuthState

    /** Verified Firebase session is active. */
    data class Authenticated(val session: AuthenticatedUser) : AuthState {
        val user: FirebaseUser? get() = session.firebaseUser
        val uid: String get() = session.uid
        val email: String? get() = session.email
    }

    /** Recoverable authentication or configuration error shown cleanly in the UI. */
    data class Error(
        val message: String,
        val isRecoverable: Boolean = true
    ) : AuthState
}

/**
 * Result of a Google Sign-In attempt.
 */
sealed interface AuthSignInOutcome {
    data class Success(val session: AuthenticatedUser) : AuthSignInOutcome
    data object Cancelled : AuthSignInOutcome
    data class Error(val message: String) : AuthSignInOutcome
}

/**
 * Result of validating an existing persisted Firebase session.
 */
enum class SessionValidationResult {
    VALID,
    OFFLINE_OR_TRANSIENT,
    EXPIRED_OR_INVALID
}

/**
 * Safely unwraps [ContextWrapper] chains to find an active, non-finishing [Activity].
 */
fun Context.findValidActivity(): Activity? {
    val visited = HashSet<Context>()
    var current: Context? = this
    while (current != null && visited.add(current)) {
        if (current is Activity) {
            return if (!current.isFinishing && !current.isDestroyed) current else null
        }
        current = if (current is ContextWrapper) current.baseContext else null
    }
    return null
}
