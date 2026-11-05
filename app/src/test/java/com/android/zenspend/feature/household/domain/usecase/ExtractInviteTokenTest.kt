package com.android.zenspend.feature.household.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtractInviteTokenTest {

    @Test
    fun `bare token is kept and trimmed`() {
        assertEquals("abc_DEF-123", extractInviteToken("  abc_DEF-123 \n"))
    }

    @Test
    fun `token is read from a join link`() {
        assertEquals("abc_DEF-123", extractInviteToken("zenspend://join?token=abc_DEF-123"))
    }

    @Test
    fun `token is read from the whole shared message`() {
        val message = "Join my ZenSpend shared cashbook:\nzenspend://join?token=abc_DEF-123\n"
        assertEquals("abc_DEF-123", extractInviteToken(message))
    }
}
