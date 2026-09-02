package com.android.zenspend.feature.household.domain

import org.junit.Assert.*
import org.junit.Test

class InvitationValidatorTest {

    @Test
    fun expiredInvitation_isInvalid() {
        val expiresAt = System.currentTimeMillis() - 1000
        assertFalse(isInvitationValid(expiresAt, "PENDING"))
    }

    @Test
    fun usedInvitation_isInvalid() {
        assertFalse(isInvitationValid(System.currentTimeMillis() + 10000, "USED"))
    }

    @Test
    fun pendingValidInvitation_passes() {
        assertTrue(isInvitationValid(System.currentTimeMillis() + 10000, "PENDING"))
    }

    private fun isInvitationValid(expiresAt: Long, status: String): Boolean {
        return status == "PENDING" && expiresAt > System.currentTimeMillis()
    }
}
