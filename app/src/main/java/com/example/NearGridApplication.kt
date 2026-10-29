package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class NearGridApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupUncaughtExceptionHandler()
        ensureFirebaseInitialized()
    }

    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("NearGridApplication", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun ensureFirebaseInitialized() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            try {
                FirebaseApp.initializeApp(this)
                Log.d("NearGridApplication", "NearGrid Application initialized successfully")
            } catch (e: Exception) {
                Log.w("NearGridApplication", "Failed auto FirebaseApp init, applying fallback options", e)
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:100000000000:android:neargriddefault")
                        .setProjectId("neargrid-app")
                        .setApiKey("AIzaSyNearGridDefaultKeyFallback12345")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                } catch (fallbackError: Exception) {
                    Log.e("NearGridApplication", "Failed fallback FirebaseApp init", fallbackError)
                }
            }
        }
    }
}
