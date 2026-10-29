package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class NearGridApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("NearGridApplication", "NearGrid Application initialized successfully")
        } catch (e: Exception) {
            Log.w("NearGridApplication", "FirebaseApp auto-initialized or error: ${e.message}")
        }
    }
}
