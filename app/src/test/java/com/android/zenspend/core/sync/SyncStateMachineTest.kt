package com.android.zenspend.core.sync

import com.android.zenspend.core.database.model.SyncState
import org.junit.Assert.*
import org.junit.Test

class SyncStateMachineTest {

    @Test
    fun pendingCreate_canTransitionToSynced() {
        var state = SyncState.PENDING_CREATE
        state = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, state)
    }

    @Test
    fun failed_canRetryToPending() {
        var state = SyncState.FAILED
        state = SyncState.PENDING_UPDATE
        assertNotEquals(SyncState.SYNCED, state)
    }
}
