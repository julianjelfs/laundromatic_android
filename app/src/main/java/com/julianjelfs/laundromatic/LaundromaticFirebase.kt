package com.julianjelfs.laundromatic

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object LaundromaticFirebase {
    private val options = FirebaseOptions.Builder()
        .setApiKey(BuildConfig.FIREBASE_API_KEY)
        .setApplicationId("1:587869683063:web:1a985df43d2f0e495e3572")
        .setProjectId("laundromatic")
        .setStorageBucket("laundromatic.appspot.com")
        .setGcmSenderId("587869683063")
        .build()

    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, options)
        }
    }
}
