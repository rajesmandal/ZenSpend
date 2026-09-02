package com.android.zenspend.core.auth.domain.usecase

import com.android.zenspend.core.auth.domain.repository.AuthRepository
import com.android.zenspend.core.common.result.Resource
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> = authRepository.signOut()
}
