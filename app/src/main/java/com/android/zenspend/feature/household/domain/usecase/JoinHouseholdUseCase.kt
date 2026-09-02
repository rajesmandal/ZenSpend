package com.android.zenspend.feature.household.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.sync.domain.repository.SyncRepository
import com.android.zenspend.feature.household.domain.model.Household
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import javax.inject.Inject

class JoinHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(token: String): Resource<Household> {
        if (token.isBlank()) {
            return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("Invalid invitation token"))
        }
        val result = householdRepository.joinHouseholdWithToken(token.trim())
        if (result is Resource.Success) {
            syncRepository.performInitialSync(result.data.id) {}
        }
        return result
    }
}
