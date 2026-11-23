package com.smartnotesai.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartNotesAIApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase first.
        Firebase.initialize(context = this)
        // Install the provider selected by the current build variant.
        AppCheckInitializer.install(this)
    }
}