package com.android.zenspend.feature.household.domain.repository

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.household.domain.model.Household
import com.android.zenspend.feature.household.domain.model.HouseholdMember
import com.android.zenspend.feature.household.domain.model.Invitation
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun observeActiveHousehold(): Flow<Household?>
    fun observeMembers(householdId: String): Flow<List<HouseholdMember>>
    suspend fun getActiveHousehold(): Household?
    suspend fun createHousehold(name: String, ownerUserId: String): Resource<Household>
    suspend fun createInvitation(householdId: String, createdByUserId: String): Resource<Invitation>
    suspend fun joinHouseholdWithToken(token: String): Resource<Household>
    suspend fun revokeInvitation(householdId: String, invitationId: String): Resource<Unit>
    suspend fun leaveHousehold(householdId: String, userId: String): Resource<Unit>
    suspend fun removeMember(householdId: String, memberUserId: String, ownerUserId: String): Resource<Unit>
    suspend fun renameHousehold(householdId: String, name: String): Resource<Unit>
    suspend fun clearLocalHouseholdCache()
}
