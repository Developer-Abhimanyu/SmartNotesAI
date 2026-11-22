package com.smartnotesai.app

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartNotesAIApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(context = this)

        /*if (BuildConfig.DEBUG) {
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        }*/

        if (BuildConfig.DEBUG) {
            Log.d("SmartNotesAI", "Installing debug provider")

            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )

            Log.d("SmartNotesAI", "Requesting App Check token")

            Firebase.appCheck.getAppCheckToken(false)
                .addOnSuccessListener { token ->
                    Log.d(
                        "SmartNotesAI",
                        "App Check token received successfully"
                    )
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "SmartNotesAI",
                        "App Check token request failed",
                        exception
                    )
                }
        }
    }
}