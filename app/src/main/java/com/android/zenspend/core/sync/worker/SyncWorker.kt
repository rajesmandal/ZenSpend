package com.android.zenspend.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.zenspend.core.sync.data.SyncEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncEngine.syncNow()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "zenspend_sync"
        private const val MAX_RETRIES = 5
    }
}
