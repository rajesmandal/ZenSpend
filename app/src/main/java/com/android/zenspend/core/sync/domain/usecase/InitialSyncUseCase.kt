package com.android.zenspend.core.sync.domain.usecase

import com.android.zenspend.core.sync.domain.model.InitialSyncProgress
import com.android.zenspend.core.sync.domain.repository.SyncRepository
import javax.inject.Inject

class InitialSyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(
        householdId: String,
        onProgress: (InitialSyncProgress) -> Unit = {}
    ) {
        syncRepository.performInitialSync(householdId, onProgress)
    }
}
