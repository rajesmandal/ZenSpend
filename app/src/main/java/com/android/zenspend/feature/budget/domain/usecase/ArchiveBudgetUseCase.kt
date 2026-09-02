package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import javax.inject.Inject

class ArchiveBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budgetId: Long) {
        repository.getBudgetById(budgetId)?.let { budget ->
            repository.updateBudget(budget.copy(isActive = false))
        }
    }
}
