package com.example.publicserviceslocator

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize Firebase explicitly to ensure it's ready before any Repository calls
        FirebaseApp.initializeApp(this)
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}