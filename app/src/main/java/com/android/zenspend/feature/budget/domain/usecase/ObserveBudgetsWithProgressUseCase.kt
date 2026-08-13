package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.model.BudgetProgress
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ObserveBudgetsWithProgressUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<BudgetProgress>> {
        return budgetRepository.getAllActiveBudgets().flatMapLatest { budgets ->
            if (budgets.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val flows = budgets.map { budget ->
                if (budget.categoryId == null) {
                    // Overall budget
                    transactionRepository.getTotalExpense(startDate, endDate).map { spent ->
                        BudgetProgress(budget = budget, spentAmount = spent)
                    }
                } else {
                    // Category budget
                    combine(
                        transactionRepository.getCategorySpending(startDate, endDate),
                        categoryRepository.getAllActiveCategories()
                    ) { spendingList, categories ->
                        val spending = spendingList.find { it.categoryId == budget.categoryId }
                        val category = categories.find { it.id == budget.categoryId }
                        BudgetProgress(
                            budget = budget,
                            spentAmount = spending?.amount ?: 0L,
                            categoryName = category?.name
                        )
                    }
                }
            }
            combine(flows) { it.toList() }
        }
    }
}
