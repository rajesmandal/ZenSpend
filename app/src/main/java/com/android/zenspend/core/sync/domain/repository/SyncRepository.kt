package com.android.zenspend.core.sync.domain.repository

import com.android.zenspend.core.sync.domain.model.InitialSyncProgress

interface SyncRepository {
    suspend fun uploadPendingChanges(householdId: String, userId: String)
    suspend fun downloadIncrementalChanges(householdId: String)
    suspend fun performInitialSync(householdId: String, onProgress: (InitialSyncProgress) -> Unit)
    suspend fun hasPendingChanges(): Boolean
}
