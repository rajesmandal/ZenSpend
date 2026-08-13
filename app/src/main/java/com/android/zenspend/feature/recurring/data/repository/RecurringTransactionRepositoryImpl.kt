package com.android.zenspend.feature.recurring.data.repository

import com.android.zenspend.core.database.dao.RecurringTransactionDao
import com.android.zenspend.feature.recurring.data.mapper.toDomain
import com.android.zenspend.feature.recurring.data.mapper.toEntity
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecurringTransactionRepositoryImpl @Inject constructor(
    private val dao: RecurringTransactionDao
) : RecurringTransactionRepository {

    override fun getAllActiveRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return dao.getAllActiveRecurringTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return dao.getAllRecurringTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecurringTransactionById(id: Long): RecurringTransaction? {
        return dao.getRecurringTransactionById(id)?.toDomain()
    }

    override suspend fun insertRecurringTransaction(recurring: RecurringTransaction): Long {
        return dao.insertRecurringTransaction(recurring.toEntity())
    }

    override suspend fun updateRecurringTransaction(recurring: RecurringTransaction) {
        dao.updateRecurringTransaction(recurring.toEntity())
    }

    override suspend fun archiveRecurringTransaction(id: Long) {
        dao.archiveRecurringTransaction(id)
    }

    override suspend fun setRecurringTransactionStatus(id: Long, isActive: Boolean) {
        dao.setRecurringTransactionStatus(id, isActive)
    }
}
