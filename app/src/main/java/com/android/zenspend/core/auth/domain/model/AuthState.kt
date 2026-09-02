package com.android.zenspend.core.auth.domain.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Authenticating : AuthState()
    data class Authenticated(
        val userId: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
