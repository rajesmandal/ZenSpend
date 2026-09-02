package com.android.zenspend.core.sync.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.zenspend.core.auth.domain.repository.AuthRepository
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.core.sync.domain.SyncScheduler
import com.android.zenspend.core.sync.domain.SyncStatusManager
import com.android.zenspend.core.sync.domain.model.SyncStatus
import com.android.zenspend.core.sync.domain.repository.SyncRepository
import com.android.zenspend.core.sync.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val scopeManager: HouseholdScopeManager,
    private val syncStatusManager: SyncStatusManager,
    @ApplicationContext private val context: Context
) : SyncScheduler {

    private val mutex = Mutex()

    suspend fun syncNow() {
        mutex.withLock {
            val user = authRepository.getCurrentUser()
            val householdId = scopeManager.activeHouseholdId.value
            if (user == null || householdId == null) {
                syncStatusManager.setStatus(SyncStatus.OFFLINE)
                return
            }
            syncStatusManager.setStatus(SyncStatus.SYNCING)
            try {
                syncRepository.uploadPendingChanges(householdId, user.userId)
                syncRepository.downloadIncrementalChanges(householdId)
                syncStatusManager.setStatus(SyncStatus.SYNCED)
            } catch (_: Exception) {
                syncStatusManager.setStatus(SyncStatus.FAILED)
            }
        }
    }

    override fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun scheduleSyncImmediate() {
        scheduleSync()
    }
}
