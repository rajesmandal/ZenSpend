package com.android.zenspend.feature.transaction.data.repository

import com.android.zenspend.core.database.dao.ExpenseDao
import com.android.zenspend.feature.transaction.data.mapper.toDomain
import com.android.zenspend.feature.transaction.data.mapper.toEntity
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        expenseDao.insertExpense(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        expenseDao.deleteExpense(transaction.toEntity())
    }
}
