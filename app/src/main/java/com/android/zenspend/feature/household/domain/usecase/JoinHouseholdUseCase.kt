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
        val result = householdRepository.joinHouseholdWithToken(extractInviteToken(token))
        if (result is Resource.Success) {
            syncRepository.performInitialSync(result.data.id) {}
        }
        return result
    }
}

private val TOKEN_IN_LINK = Regex("token=([A-Za-z0-9_-]+)")

/** Accepts a bare token, a `zenspend://join?token=...` link, or the whole shared message. */
fun extractInviteToken(raw: String): String =
    TOKEN_IN_LINK.find(raw)?.groupValues?.get(1) ?: raw.trim()
