package com.android.zenspend.core.firebase

import android.content.Context
import com.android.zenspend.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Ensures Firebase is initialized before Hilt injects [com.google.firebase.auth.FirebaseAuth].
 *
 * When [google-services.json] is present, the Google Services plugin auto-initializes Firebase.
 * Without it, we initialize with a local placeholder config so the app can run (emulator / offline).
 */
object FirebaseBootstrap {

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) return

        if (BuildConfig.FIREBASE_CONFIGURED) {
            FirebaseApp.initializeApp(context)
            return
        }

        // Placeholder options — sufficient for Auth/Firestore emulators and offline-only use.
        val options = FirebaseOptions.Builder()
            .setProjectId("zenspend-local")
            .setApplicationId("1:000000000000:android:0000000000000000000000")
            .setApiKey("local-dev-api-key")
            .build()
        FirebaseApp.initializeApp(context, options)
    }
}
