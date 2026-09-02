package com.android.zenspend.core.auth.domain.repository

import android.content.Intent
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.model.AuthUser
import com.android.zenspend.core.common.result.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signInWithGoogle(credential: String): Resource<AuthUser>
    suspend fun signOut(): Resource<Unit>
    fun getGoogleSignInIntent(): Intent?
}
