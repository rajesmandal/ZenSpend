package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.model.BudgetPlanResult
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Turns a [BudgetPlanResult] into real budgets for one month: an overall budget (the spending cap)
 * plus one budget per assigned category. Budgets that already exist for that month are updated in
 * place, so applying a plan twice never creates duplicates.
 */
class ApplyBudgetPlanUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    /** @return how many budgets were created or updated. */
    suspend operator fun invoke(result: BudgetPlanResult, startDate: Long, endDate: Long): Int {
        val existing = repository.getAllActiveBudgets().first().filter { it.startDate == startDate }

        // null = overall budget
        val targets: Map<Long?, Long> = buildMap {
            if (result.spendingCap > 0) put(null, result.spendingCap)
            result.categoryAmounts.forEach { (categoryId, amount) -> if (amount > 0) put(categoryId, amount) }
        }

        targets.forEach { (categoryId, amount) ->
            val current = existing.find { it.categoryId == categoryId }
            if (current != null) {
                repository.updateBudget(current.copy(amount = amount))
            } else {
                repository.insertBudget(
                    Budget(categoryId = categoryId, amount = amount, startDate = startDate, endDate = endDate)
                )
            }
        }
        return targets.size
    }
}
