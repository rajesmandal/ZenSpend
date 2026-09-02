package com.android.zenspend.core.auth.domain.model

data class AuthUser(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)
