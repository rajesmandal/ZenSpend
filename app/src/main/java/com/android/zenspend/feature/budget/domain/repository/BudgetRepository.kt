package com.android.zenspend.feature.budget.domain.repository

import com.android.zenspend.feature.budget.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllActiveBudgets(): Flow<List<Budget>>
    suspend fun getBudgetById(id: Long): Budget?
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
}
