package com.android.zenspend.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.android.zenspend.core.database.entity.SplitExpenseEntity
import com.android.zenspend.core.database.entity.SplitGroupEntity
import com.android.zenspend.core.database.entity.SplitMemberEntity
import com.android.zenspend.core.database.entity.SplitSettlementEntity
import com.android.zenspend.core.database.entity.SplitShareEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SplitDao {
    // ---- groups ----
    @Query("SELECT * FROM split_groups ORDER BY id DESC")
    abstract fun observeGroups(): Flow<List<SplitGroupEntity>>

    @Query("SELECT * FROM split_groups WHERE id = :id")
    abstract fun observeGroup(id: Long): Flow<SplitGroupEntity?>

    @Insert
    abstract suspend fun insertGroup(group: SplitGroupEntity): Long

    @Query("DELETE FROM split_groups WHERE id = :id")
    abstract suspend fun deleteGroup(id: Long)

    @Query("UPDATE split_groups SET name = :name WHERE id = :id")
    abstract suspend fun renameGroup(id: Long, name: String)

    // ---- members ----
    @Query("SELECT * FROM split_members ORDER BY id ASC")
    abstract fun observeAllMembers(): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_members WHERE groupId = :groupId ORDER BY isMe DESC, id ASC")
    abstract fun observeMembers(groupId: Long): Flow<List<SplitMemberEntity>>

    @Query("SELECT id FROM split_members WHERE groupId = :groupId")
    abstract suspend fun getMemberIds(groupId: Long): List<Long>

    @Insert
    abstract suspend fun insertMember(member: SplitMemberEntity): Long

    @Query("DELETE FROM split_members WHERE id = :id AND isMe = 0")
    abstract suspend fun deleteMember(id: Long)

    @Query(
        "SELECT (SELECT COUNT(*) FROM split_expenses WHERE paidBy = :memberId) + " +
            "(SELECT COUNT(*) FROM split_shares WHERE memberId = :memberId) + " +
            "(SELECT COUNT(*) FROM split_settlements WHERE fromMember = :memberId OR toMember = :memberId)"
    )
    abstract suspend fun activityCount(memberId: Long): Int

    // ---- expenses ----
    @Query("SELECT * FROM split_expenses ORDER BY date DESC, id DESC")
    abstract fun observeAllExpenses(): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY date DESC, id DESC")
    abstract fun observeExpenses(groupId: Long): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE id = :id")
    abstract suspend fun getExpense(id: Long): SplitExpenseEntity?

    @Insert
    abstract suspend fun insertExpense(expense: SplitExpenseEntity): Long

    @Query("DELETE FROM split_expenses WHERE id = :id")
    abstract suspend fun deleteExpense(id: Long)

    @Query(
        "UPDATE split_expenses SET title = :title, amount = :amount, paidBy = :paidBy, date = :date, " +
            "splitType = :splitType, note = :note WHERE id = :id"
    )
    abstract suspend fun updateExpense(
        id: Long, title: String, amount: Long, paidBy: Long, date: Long, splitType: String, note: String?
    )

    // ---- shares ----
    @Query("SELECT * FROM split_shares")
    abstract fun observeAllShares(): Flow<List<SplitShareEntity>>

    @Query(
        "SELECT s.* FROM split_shares s INNER JOIN split_expenses e ON e.id = s.expenseId WHERE e.groupId = :groupId"
    )
    abstract fun observeShares(groupId: Long): Flow<List<SplitShareEntity>>

    @Query("SELECT * FROM split_shares WHERE expenseId = :expenseId")
    abstract suspend fun getShares(expenseId: Long): List<SplitShareEntity>

    @Insert
    abstract suspend fun insertShares(shares: List<SplitShareEntity>)

    @Query("DELETE FROM split_shares WHERE expenseId = :expenseId")
    abstract suspend fun deleteShares(expenseId: Long)

    // ---- settlements ----
    @Query("SELECT * FROM split_settlements ORDER BY date DESC, id DESC")
    abstract fun observeAllSettlements(): Flow<List<SplitSettlementEntity>>

    @Query("SELECT * FROM split_settlements WHERE groupId = :groupId ORDER BY date DESC, id DESC")
    abstract fun observeSettlements(groupId: Long): Flow<List<SplitSettlementEntity>>

    @Insert
    abstract suspend fun insertSettlement(settlement: SplitSettlementEntity): Long

    @Query("DELETE FROM split_settlements WHERE id = :id")
    abstract suspend fun deleteSettlement(id: Long)

    // ---- multi-step writes ----

    /** Group and its members (the phone owner first) land together or not at all. */
    @Transaction
    open suspend fun createGroup(name: String, myName: String, others: List<String>): Long {
        val groupId = insertGroup(SplitGroupEntity(name = name))
        insertMember(SplitMemberEntity(groupId = groupId, name = myName, isMe = true))
        others.forEach { insertMember(SplitMemberEntity(groupId = groupId, name = it)) }
        return groupId
    }

    /** Expense and its shares are written as one step so a share never outlives its bill. */
    @Transaction
    open suspend fun saveExpense(expense: SplitExpenseEntity, shares: Map<Long, Long>): Long {
        val id = if (expense.id == 0L) {
            insertExpense(expense)
        } else {
            updateExpense(
                expense.id, expense.title, expense.amount, expense.paidBy, expense.date, expense.splitType, expense.note
            )
            deleteShares(expense.id)
            expense.id
        }
        insertShares(shares.map { (member, amount) -> SplitShareEntity(expenseId = id, memberId = member, amount = amount) })
        return id
    }

    /** Removes a member only when they never took part in anything. Returns false when blocked. */
    @Transaction
    open suspend fun removeMemberIfUnused(memberId: Long): Boolean {
        if (activityCount(memberId) > 0) return false
        deleteMember(memberId)
        return true
    }
}
