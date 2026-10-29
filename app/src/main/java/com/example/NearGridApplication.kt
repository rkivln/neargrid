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
                Log.d("NearGridApplication", "FirebaseApp auto-initialized successfully")
            } catch (e: Exception) {
                Log.w("NearGridApplication", "Auto FirebaseApp init failed, applying valid project options", e)
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:253737365034:android:858656ab161612d795dd53")
                        .setProjectId("gen-lang-client-0509703417")
                        .setApiKey("AIzaSyBSc8bWTsIYVL8Sj7kicx9zSZLOAcLD1LA")
                        .setGcmSenderId("253737365034")
                        .setStorageBucket("gen-lang-client-0509703417.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.d("NearGridApplication", "FirebaseApp initialized with verified project options")
                } catch (fallbackError: Exception) {
                    Log.e("NearGridApplication", "Fatal error initializing FirebaseApp", fallbackError)
                }
            }
        }
    }
}
