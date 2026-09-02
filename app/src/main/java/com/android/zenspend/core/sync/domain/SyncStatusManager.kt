package com.android.zenspend.core.sync.domain

import com.android.zenspend.core.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncStatusManager {
    val status: Flow<SyncStatus>
    suspend fun setStatus(status: SyncStatus)
}
