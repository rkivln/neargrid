package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.data.remote.FirebaseBootstrap
import com.google.firebase.firestore.FieldValue
import java.util.Date
import java.util.UUID
import kotlin.concurrent.thread

/**
 * Silent Cloud Crash & Telemetry Reporting Manager.
 * Catches uncaught JVM exceptions, coroutine failures, and non-fatal repository errors,
 * sending silent diagnostic telemetry logs to the named Firestore database when authenticated.
 */
object CrashTelemetryManager {
    private const val TAG = "CrashTelemetryManager"
    private const val APP_VERSION = "v1.0.3"
    private var isInitialized = false
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext ?: context
        if (isInitialized) return
        isInitialized = true

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logFatalCrash(thread, throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log crash telemetry silently", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun logFatalCrash(thread: Thread, throwable: Throwable) {
        val payload = buildTelemetryPayload(
            severity = "FATAL",
            errorName = throwable.javaClass.simpleName,
            errorMessage = throwable.message ?: "Uncaught Exception in thread ${thread.name}",
            stackTrace = throwable.stackTraceToString()
        )
        sendTelemetrySilently(payload)
    }

    fun logNonFatalException(context: String, throwable: Throwable) {
        val payload = buildTelemetryPayload(
            severity = "NON_FATAL",
            errorName = throwable.javaClass.simpleName,
            errorMessage = "[$context] ${throwable.message ?: "Non-fatal operational error"}",
            stackTrace = throwable.stackTraceToString()
        )
        sendTelemetrySilently(payload)
    }

    fun logEvent(eventName: String, details: Map<String, Any> = emptyMap()) {
        val payload = buildTelemetryPayload(
            severity = "INFO",
            errorName = eventName,
            errorMessage = "Operational telemetry event: $eventName",
            stackTrace = details.toString()
        )
        sendTelemetrySilently(payload)
    }

    fun buildTelemetryPayload(
        severity: String,
        errorName: String,
        errorMessage: String,
        stackTrace: String
    ): Map<String, Any> {
        val ctx = appContext
        val uid = if (ctx != null) {
            try {
                FirebaseBootstrap.getAuth(ctx)?.currentUser?.uid ?: "anonymous"
            } catch (_: Exception) {
                "anonymous"
            }
        } else {
            "anonymous"
        }
        val truncatedStackTrace = stackTrace.lines().take(15).joinToString("\n")

        return mapOf(
            "logId" to UUID.randomUUID().toString(),
            "appVersion" to APP_VERSION,
            "severity" to severity,
            "errorName" to errorName.take(100),
            "errorMessage" to errorMessage.take(1000),
            "stackTraceSummary" to truncatedStackTrace.take(3000),
            "userId" to uid,
            "deviceModel" to "${Build.MANUFACTURER ?: "unknown"} ${Build.MODEL ?: "unknown"}".trim().take(120),
            "androidVersion" to (Build.VERSION.RELEASE ?: "unknown").take(40),
            "sdkInt" to Build.VERSION.SDK_INT,
            "timestamp" to Date().time
        )
    }

    private fun sendTelemetrySilently(payload: Map<String, Any>) {
        val ctx = appContext ?: return
        val uid = try {
            FirebaseBootstrap.getAuth(ctx)?.currentUser?.uid
        } catch (_: Exception) {
            null
        }
        if (uid.isNullOrBlank()) return

        thread {
            try {
                val db = FirebaseBootstrap.getFirestore(ctx) ?: return@thread
                val logId = (payload["logId"] as? String)?.takeIf { it.isNotBlank() }
                    ?: UUID.randomUUID().toString()
                val firestorePayload = HashMap(payload).apply {
                    put("userId", uid)
                    put("createdAt", FieldValue.serverTimestamp())
                }
                db.collection("telemetry_logs")
                    .document(logId)
                    .set(firestorePayload)
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Silent telemetry upload unsuccessful: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Telemetry dispatch error: ${e.message}")
            }
        }
    }
}
