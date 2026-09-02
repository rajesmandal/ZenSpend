package com.android.zenspend.core.sync.data

import com.android.zenspend.core.database.model.SyncState
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.core.sync.domain.SyncScheduler
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncWriteCoordinator @Inject constructor(
    private val scopeManager: HouseholdScopeManager,
    private val syncScheduler: SyncScheduler,
    private val remoteChangeGuard: RemoteChangeGuard
) {
    fun shouldSync(householdId: String?): Boolean = householdId != null

    fun prepareCreateMetadata(userId: String, householdId: String?): SyncCreateMetadata? {
        if (householdId == null) return null
        val now = System.currentTimeMillis()
        return SyncCreateMetadata(
            remoteId = UUID.randomUUID().toString(),
            householdId = householdId,
            createdByUserId = userId,
            updatedByUserId = userId,
            createdAt = now,
            updatedAt = now,
            syncState = SyncState.PENDING_CREATE,
            isDeleted = false
        )
    }

    fun prepareUpdateMetadata(
        existingHouseholdId: String?,
        existingRemoteId: String?,
        existingCreatedByUserId: String?,
        userId: String
    ): SyncUpdateMetadata? {
        if (existingHouseholdId == null) return null
        return SyncUpdateMetadata(
            updatedByUserId = userId,
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING_UPDATE
        )
    }

    fun prepareDeleteMetadata(userId: String, householdId: String?): SyncDeleteMetadata? {
        if (householdId == null) return null
        return SyncDeleteMetadata(
            isDeleted = true,
            updatedByUserId = userId,
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING_DELETE
        )
    }

    fun onSharedWriteScheduled() {
        if (!remoteChangeGuard.isApplyingRemoteChange) {
            syncScheduler.scheduleSync()
        }
    }

    fun activeHouseholdIdForWrite(): String? {
        return if (scopeManager.isPersonalMode()) null else scopeManager.activeHouseholdId.value
    }
}

data class SyncCreateMetadata(
    val remoteId: String,
    val householdId: String,
    val createdByUserId: String,
    val updatedByUserId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState,
    val isDeleted: Boolean
)

data class SyncUpdateMetadata(
    val updatedByUserId: String,
    val updatedAt: Long,
    val syncState: SyncState
)

data class SyncDeleteMetadata(
    val isDeleted: Boolean,
    val updatedByUserId: String,
    val updatedAt: Long,
    val syncState: SyncState
)
