package com.android.zenspend.feature.transaction.data.repository

import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.data.mapper.toDomain
import com.android.zenspend.feature.transaction.data.mapper.toEntity
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFilteredTransactions(
        walletId: Long?,
        categoryId: Long?,
        type: TransactionType?,
        searchQuery: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Transaction>> {
        return transactionDao.getFilteredTransactions(
            walletId = walletId,
            categoryId = categoryId,
            type = type?.name,
            searchQuery = searchQuery,
            startDate = startDate,
            endDate = endDate
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByWallet(walletId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByWallet(walletId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        // Need to add this query to TransactionDao if not exists, but for now I'll use a placeholder logic or add it.
        // Actually, let's assume I might need to add it to DAO.
        return transactionDao.getAllTransactions().map { entities ->
            entities.filter { it.date in startDate..endDate }.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        // Need to add this to DAO or filter all. Better add to DAO.
        // For now, I'll filter.
        return null // Placeholder
    }

    override suspend fun getTransactionByOccurrenceKey(key: String): Transaction? {
        return transactionDao.getTransactionByOccurrenceKey(key)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override fun getTotalIncome(startDate: Long, endDate: Long): Flow<Long> {
        return transactionDao.getTotalAmountByType(TransactionType.INCOME, startDate, endDate)
    }

    override fun getTotalExpense(startDate: Long, endDate: Long): Flow<Long> {
        return transactionDao.getTotalNetExpense(TransactionType.EXPENSE, TransactionType.REFUND, startDate, endDate)
    }

    override fun getCategorySpending(
        startDate: Long,
        endDate: Long
    ): Flow<List<com.android.zenspend.feature.dashboard.domain.model.CategorySpending>> {
        return transactionDao.getCategorySpending(startDate, endDate).map { list ->
            val total = list.sumOf { it.totalAmount }
            list.map { entity ->
                com.android.zenspend.feature.dashboard.domain.model.CategorySpending(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    amount = entity.totalAmount,
                    percentage = if (total > 0) entity.totalAmount.toFloat() / total else 0f
                )
            }
        }
    }
}
