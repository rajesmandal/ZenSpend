package com.android.zenspend.core.auth.domain.usecase

import com.android.zenspend.core.auth.domain.model.AuthUser
import com.android.zenspend.core.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? = authRepository.getCurrentUser()
}
