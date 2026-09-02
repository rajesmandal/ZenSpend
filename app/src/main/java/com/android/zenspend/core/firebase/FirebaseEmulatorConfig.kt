package com.android.zenspend.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Connects Firebase services to local emulators when [useEmulators] is true.
 * Set via BuildConfig or debug flag.
 */
@Singleton
class FirebaseEmulatorConfig @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    fun connectEmulatorsIfNeeded(useEmulators: Boolean) {
        if (!useEmulators) return
        try {
            auth.useEmulator("10.0.2.2", 9099)
            firestore.useEmulator("10.0.2.2", 8080)
            functions.useEmulator("10.0.2.2", 5001)
        } catch (_: IllegalStateException) {
            // Already connected
        }
    }
}
