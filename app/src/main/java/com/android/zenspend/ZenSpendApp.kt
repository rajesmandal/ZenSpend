package com.android.zenspend

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.android.zenspend.core.firebase.FirebaseBootstrap
import com.android.zenspend.core.firebase.FirebaseEmulatorConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZenSpendApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var emulatorConfig: FirebaseEmulatorConfig

    override fun onCreate() {
        FirebaseBootstrap.init(this)
        super.onCreate()
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            emulatorConfig.connectEmulatorsIfNeeded(true)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}