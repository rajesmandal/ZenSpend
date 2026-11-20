package com.android.zenspend.feature.split.domain.repository

import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitGroupDetails
import com.android.zenspend.feature.split.domain.model.SplitGroupOverview
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import kotlinx.coroutines.flow.Flow

interface SplitRepository {
    fun observeGroups(): Flow<List<SplitGroupOverview>>
    fun observeGroup(id: Long): Flow<SplitGroupDetails?>

    /** Creates the group with the phone owner ([myName]) plus [others]. Blank names are skipped. */
    suspend fun createGroup(name: String, myName: String, others: List<String>): Long
    suspend fun deleteGroup(id: Long)

    suspend fun addMember(groupId: Long, name: String): Boolean

    /** Only possible while the member has no expense, share or settlement. Returns false when blocked. */
    suspend fun removeMember(memberId: Long): Boolean

    suspend fun getExpense(id: Long): SplitExpense?

    /**
     * Stores a bill. Returns null (and writes nothing) when it is inconsistent: non-positive amount,
     * shares not adding up to the amount, or the payer / a participant is not in the group.
     */
    suspend fun saveExpense(expense: SplitExpense): Long?
    suspend fun deleteExpense(id: Long)

    /** Returns false (and writes nothing) for a non-positive amount, paying yourself, or non-members. */
    suspend fun addSettlement(settlement: SplitSettlement): Boolean
    suspend fun deleteSettlement(id: Long)
}
