package com.julianjelfs.laundromatic

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LaundromaticApplication : Application(), Configuration.Provider {
    lateinit var repository: LaundromaticRepository
        private set

    override fun onCreate() {
        super.onCreate()
        LaundromaticFirebase.initialize(this)

        Firebase.firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()

        repository = LaundromaticRepository(
            auth = Firebase.auth,
            firestore = Firebase.firestore,
        )

        DailyReminderWorker.createChannel(this)
        DailyReminderWorker.cancelLegacyPeriodicWork(this)
        DailyReminderAlarm.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() {
            val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            return Configuration.Builder()
                .setMinimumLoggingLevel(if (debuggable) Log.DEBUG else Log.INFO)
                .build()
        }
}
