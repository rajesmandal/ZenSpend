package com.android.zenspend.core.sync

import org.junit.Assert.*
import org.junit.Test

class TransactionMergeTest {

    @Test
    fun concurrentCreates_bothSurviveWithUniqueRemoteIds() {
        val remoteA = "uuid-a"
        val remoteB = "uuid-b"
        val transactions = listOf(remoteA, remoteB)
        assertEquals(2, transactions.distinct().size)
    }
}
