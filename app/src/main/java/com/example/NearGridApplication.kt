package com.example

import android.app.Application
import android.util.Log
import com.example.data.remote.FirebaseBootstrap

class NearGridApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupUncaughtExceptionHandler()
        val app = FirebaseBootstrap.ensureInitialized(this)
        if (app == null) {
            Log.e("NearGridApplication", "FirebaseApp could not be initialized on startup")
        }
    }

    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("NearGridApplication", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
