package com.android.zenspend.feature.household.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import javax.inject.Inject

class RevokeInvitationUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String, invitationId: String): Resource<Unit> =
        householdRepository.revokeInvitation(householdId, invitationId)
}
