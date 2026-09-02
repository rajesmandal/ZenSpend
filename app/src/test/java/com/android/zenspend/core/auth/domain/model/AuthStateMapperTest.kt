package com.android.zenspend.core.auth.domain.model

import org.junit.Assert.*
import org.junit.Test

class AuthStateMapperTest {

    @Test
    fun unauthenticated_hasNoUserId() {
        val state: AuthState = AuthState.Unauthenticated
        assertTrue(state !is AuthState.Authenticated)
    }

    @Test
    fun authenticated_exposesSafeFieldsOnly() {
        val state = AuthState.Authenticated(
            userId = "uid123",
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = null
        )
        assertEquals("uid123", state.userId)
        assertNull(state.photoUrl)
    }
}
