package com.android.zenspend.feature.transaction.domain.repository

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getFilteredTransactions(
        walletId: Long? = null,
        categoryId: Long? = null,
        type: TransactionType? = null,
        searchQuery: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<Transaction>>
    
    fun getTransactionsByWallet(walletId: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun getTransactionByOccurrenceKey(key: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Long>
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Long>
    fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<com.android.zenspend.feature.dashboard.domain.model.CategorySpending>>
}
