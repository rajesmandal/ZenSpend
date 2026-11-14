package com.android.zenspend

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.android.zenspend.core.firebase.FirebaseBootstrap
import com.android.zenspend.core.firebase.FirebaseEmulatorConfig
import dagger.hilt.android.HiltAndroidApp
import com.android.zenspend.core.quickadd.QuickAddNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ZenSpendApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var emulatorConfig: FirebaseEmulatorConfig

    @Inject
    lateinit var quickAddNotifier: QuickAddNotifier

    override fun onCreate() {
        FirebaseBootstrap.init(this)
        super.onCreate()
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            emulatorConfig.connectEmulatorsIfNeeded(true)
        }
        // Bring the pinned quick-add notification back (or remove it) to match the setting.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch { quickAddNotifier.sync() }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}