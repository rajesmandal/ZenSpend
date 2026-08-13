package com.android.zenspend.feature.budget.domain.model

data class BudgetProgress(
    val budget: Budget,
    val spentAmount: Long,
    val categoryName: String? = null
) {
    val remainingAmount: Long = budget.amount - spentAmount
    val progress: Float = if (budget.amount > 0) spentAmount.toFloat() / budget.amount else 0f
    val isOverBudget: Boolean = spentAmount > budget.amount
}
