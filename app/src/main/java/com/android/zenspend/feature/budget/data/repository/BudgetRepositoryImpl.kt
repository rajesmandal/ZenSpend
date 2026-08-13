package com.android.zenspend.feature.budget.data.repository

import com.android.zenspend.core.database.dao.BudgetDao
import com.android.zenspend.feature.budget.data.mapper.toDomain
import com.android.zenspend.feature.budget.data.mapper.toEntity
import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {
    override fun getAllActiveBudgets(): Flow<List<Budget>> =
        budgetDao.getAllActiveBudgets().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getBudgetById(id: Long): Budget? =
        null // TODO: Implement in DAO if needed

    override suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget.toEntity())

    override suspend fun updateBudget(budget: Budget) =
        budgetDao.updateBudget(budget.toEntity())

    override suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget.toEntity())
}
