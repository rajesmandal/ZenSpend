package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Resource<Long> {
        // 1. Validation
        if (budget.amount <= 0) {
            return Resource.Error(AppError.ValidationError.InvalidAmount)
        }
        
        if (budget.categoryId != null) {
            // Check for overlap in same category + period
            val existing = repository.getAllActiveBudgets().first().find { 
                it.categoryId == budget.categoryId && it.startDate == budget.startDate 
            }
            if (existing != null) {
                return Resource.Error(AppError.ValidationError.GenericError("Budget already exists for this category and month"))
            }
        } else {
            // Check for overlap in overall budget
            val existing = repository.getAllActiveBudgets().first().find { 
                it.categoryId == null && it.startDate == budget.startDate 
            }
            if (existing != null) {
                return Resource.Error(AppError.ValidationError.GenericError("Overall budget already exists for this month"))
            }
        }

        return try {
            val id = repository.insertBudget(budget)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(AppError.DatabaseError.GenericError(e.message ?: "Unknown database error"))
        }
    }
}
