package com.android.zenspend.core.auth.domain.usecase

import com.android.zenspend.core.auth.domain.model.AuthUser
import com.android.zenspend.core.auth.domain.repository.AuthRepository
import com.android.zenspend.core.common.result.Resource
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Resource<AuthUser> =
        authRepository.signInWithGoogle(idToken)
}
