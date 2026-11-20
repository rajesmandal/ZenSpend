package com.android.zenspend.feature.split.data

import com.android.zenspend.core.database.dao.SplitDao
import com.android.zenspend.core.database.entity.SplitExpenseEntity
import com.android.zenspend.core.database.entity.SplitGroupEntity
import com.android.zenspend.core.database.entity.SplitMemberEntity
import com.android.zenspend.core.database.entity.SplitSettlementEntity
import com.android.zenspend.core.database.entity.SplitShareEntity
import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitGroup
import com.android.zenspend.feature.split.domain.model.SplitGroupDetails
import com.android.zenspend.feature.split.domain.model.SplitGroupOverview
import com.android.zenspend.feature.split.domain.model.SplitMember
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import com.android.zenspend.feature.split.domain.model.SplitType
import com.android.zenspend.feature.split.domain.repository.SplitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SplitRepositoryImpl @Inject constructor(
    private val dao: SplitDao
) : SplitRepository {

    override fun observeGroups(): Flow<List<SplitGroupOverview>> = combine(
        dao.observeGroups(), dao.observeAllMembers(), dao.observeAllExpenses(),
        dao.observeAllShares(), dao.observeAllSettlements()
    ) { groups, members, expenses, shares, settlements ->
        val sharesByExpense = shares.groupBy { it.expenseId }
        val membersByGroup = members.groupBy { it.groupId }
        val expensesByGroup = expenses.groupBy { it.groupId }
        val settlementsByGroup = settlements.groupBy { it.groupId }
        groups.map { g ->
            SplitGroupOverview(
                SplitGroupDetails(
                    group = g.toDomain(),
                    members = membersByGroup[g.id].orEmpty().map { it.toDomain() },
                    expenses = expensesByGroup[g.id].orEmpty().map { it.toDomain(sharesByExpense[it.id].orEmpty()) },
                    settlements = settlementsByGroup[g.id].orEmpty().map { it.toDomain() }
                )
            )
        }
    }

    override fun observeGroup(id: Long): Flow<SplitGroupDetails?> = combine(
        dao.observeGroup(id), dao.observeMembers(id), dao.observeExpenses(id),
        dao.observeShares(id), dao.observeSettlements(id)
    ) { group, members, expenses, shares, settlements ->
        group?.let {
            val sharesByExpense = shares.groupBy { s -> s.expenseId }
            SplitGroupDetails(
                group = it.toDomain(),
                members = members.map { m -> m.toDomain() },
                expenses = expenses.map { e -> e.toDomain(sharesByExpense[e.id].orEmpty()) },
                settlements = settlements.map { s -> s.toDomain() }
            )
        }
    }

    override suspend fun createGroup(name: String, myName: String, others: List<String>): Long =
        dao.createGroup(
            name = name.trim(),
            myName = myName.trim().ifBlank { DEFAULT_ME },
            others = others.map { it.trim() }.filter { it.isNotEmpty() }
        )

    override suspend fun deleteGroup(id: Long) = dao.deleteGroup(id)

    override suspend fun addMember(groupId: Long, name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        dao.insertMember(SplitMemberEntity(groupId = groupId, name = clean))
        return true
    }

    override suspend fun removeMember(memberId: Long): Boolean = dao.removeMemberIfUnused(memberId)

    override suspend fun getExpense(id: Long): SplitExpense? =
        dao.getExpense(id)?.toDomain(dao.getShares(id))

    override suspend fun saveExpense(expense: SplitExpense): Long? {
        val shares = expense.shares.filterValues { it > 0 }
        if (expense.amount <= 0 || shares.isEmpty() || shares.values.sum() != expense.amount) return null
        val members = dao.getMemberIds(expense.groupId).toSet()
        if (expense.paidBy !in members || !members.containsAll(shares.keys)) return null
        // An edit may only touch an expense of the same group.
        if (expense.id != 0L && dao.getExpense(expense.id)?.groupId != expense.groupId) return null
        return dao.saveExpense(
            SplitExpenseEntity(
                id = expense.id,
                groupId = expense.groupId,
                title = expense.title.trim().ifBlank { DEFAULT_TITLE },
                amount = expense.amount,
                paidBy = expense.paidBy,
                date = expense.date,
                splitType = expense.splitType.name,
                note = expense.note?.trim()?.ifBlank { null }
            ),
            shares
        )
    }

    override suspend fun deleteExpense(id: Long) = dao.deleteExpense(id)

    override suspend fun addSettlement(settlement: SplitSettlement): Boolean {
        if (settlement.amount <= 0 || settlement.from == settlement.to) return false
        val members = dao.getMemberIds(settlement.groupId).toSet()
        if (settlement.from !in members || settlement.to !in members) return false
        dao.insertSettlement(
            SplitSettlementEntity(
                groupId = settlement.groupId,
                fromMember = settlement.from,
                toMember = settlement.to,
                amount = settlement.amount,
                date = settlement.date
            )
        )
        return true
    }

    override suspend fun deleteSettlement(id: Long) = dao.deleteSettlement(id)

    private companion object {
        const val DEFAULT_ME = "You"
        const val DEFAULT_TITLE = "Expense"
    }
}

private fun SplitGroupEntity.toDomain() = SplitGroup(id, name, createdAt)
private fun SplitMemberEntity.toDomain() = SplitMember(id, groupId, name, isMe)
private fun SplitSettlementEntity.toDomain() = SplitSettlement(id, groupId, fromMember, toMember, amount, date)
private fun SplitExpenseEntity.toDomain(shares: List<SplitShareEntity>) = SplitExpense(
    id = id, groupId = groupId, title = title, amount = amount, paidBy = paidBy, date = date,
    shares = shares.associate { it.memberId to it.amount },
    splitType = runCatching { SplitType.valueOf(splitType) }.getOrDefault(SplitType.EQUAL),
    note = note
)
