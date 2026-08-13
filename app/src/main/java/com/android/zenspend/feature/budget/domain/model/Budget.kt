package com.android.zenspend.feature.budget.domain.model

import com.android.zenspend.core.database.model.BudgetPeriod

data class Budget(
    val id: Long = 0,
    val categoryId: Long?, // Null means overall budget
    val amount: Long,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)
