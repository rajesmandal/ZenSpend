package com.android.zenspend.core.auth.domain.usecase

import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.observeAuthState()
}
