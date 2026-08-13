package com.android.zenspend.feature.recurring.domain.repository

import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun getAllActiveRecurringTransactions(): Flow<List<RecurringTransaction>>
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction?
    suspend fun insertRecurringTransaction(recurring: RecurringTransaction): Long
    suspend fun updateRecurringTransaction(recurring: RecurringTransaction)
    suspend fun archiveRecurringTransaction(id: Long)
    suspend fun setRecurringTransactionStatus(id: Long, isActive: Boolean)
}
