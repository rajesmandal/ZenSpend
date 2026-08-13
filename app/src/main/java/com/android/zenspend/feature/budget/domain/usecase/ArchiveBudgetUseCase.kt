package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import javax.inject.Inject

class ArchiveBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: com.android.zenspend.feature.budget.domain.model.Budget) {
        repository.updateBudget(budget.copy(isActive = false))
    }
}
