package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND isDeleted = 0")
    fun getAllActiveRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun updateRecurringTransaction(recurring: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isDeleted = 1, isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveRecurringTransaction(id: Long, updatedAt: Long)

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun setRecurringTransactionStatus(id: Long, isActive: Boolean)
}
