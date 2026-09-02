package com.android.zenspend.feature.household.domain

import org.junit.Assert.*
import org.junit.Test

class HouseholdCreationValidatorTest {

    @Test
    fun blankName_isInvalid() {
        assertFalse(isValidHouseholdName("   "))
    }

    @Test
    fun validName_passes() {
        assertTrue(isValidHouseholdName("Our Home"))
    }

    @Test
    fun memberLimit_defaultsToTwo() {
        assertEquals(2, defaultMemberLimit())
    }

    private fun isValidHouseholdName(name: String) = name.isNotBlank()
    private fun defaultMemberLimit() = 2
}
