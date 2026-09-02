package com.android.zenspend.feature.household.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.monetization.domain.EntitlementManager
import com.android.zenspend.feature.household.domain.model.Household
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val entitlementManager: EntitlementManager
) {
    suspend operator fun invoke(name: String, ownerUserId: String): Resource<Household> {
        if (name.isBlank()) {
            return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("Household name required"))
        }
        val isPremium = entitlementManager.isPremium.first()
        if (!isPremium) {
            return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("Premium required to create shared cashbook"))
        }
        return householdRepository.createHousehold(name, ownerUserId)
    }
}
