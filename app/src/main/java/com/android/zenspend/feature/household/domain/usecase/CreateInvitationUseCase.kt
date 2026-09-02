package com.android.zenspend.feature.household.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.household.domain.model.Invitation
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import javax.inject.Inject

class CreateInvitationUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String, createdByUserId: String): Resource<Invitation> =
        householdRepository.createInvitation(householdId, createdByUserId)
}
