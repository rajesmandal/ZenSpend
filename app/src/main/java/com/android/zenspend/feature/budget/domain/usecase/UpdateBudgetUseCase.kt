package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget) = repository.updateBudget(budget)
}
