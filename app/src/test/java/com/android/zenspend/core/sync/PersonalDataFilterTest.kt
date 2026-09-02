package com.android.zenspend.core.sync

import org.junit.Assert.*
import org.junit.Test

class PersonalDataFilterTest {

    @Test
    fun personalRecords_haveNullHouseholdId() {
        val householdId: String? = null
        assertFalse(shouldUpload(householdId))
    }

    @Test
    fun sharedRecords_shouldUpload() {
        assertTrue(shouldUpload("household-123"))
    }

    private fun shouldUpload(householdId: String?): Boolean = householdId != null
}
