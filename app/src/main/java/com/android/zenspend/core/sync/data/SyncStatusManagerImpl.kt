package com.android.zenspend.core.sync.data

import com.android.zenspend.core.sync.domain.SyncStatusManager
import com.android.zenspend.core.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStatusManagerImpl @Inject constructor() : SyncStatusManager {
    private val _status = MutableStateFlow(SyncStatus.SYNCED)
    override val status: Flow<SyncStatus> = _status.asStateFlow()

    override suspend fun setStatus(status: SyncStatus) {
        _status.value = status
    }
}
