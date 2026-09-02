package com.android.zenspend.core.sync.domain

interface SyncScheduler {
    fun scheduleSync()
    fun scheduleSyncImmediate()
}
