package com.android.zenspend.feature.budget.data.mapper

import com.android.zenspend.core.database.entity.BudgetEntity
import com.android.zenspend.feature.budget.domain.model.Budget

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    amount = amount,
    period = period,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    categoryId = categoryId,
    amount = amount,
    period = period,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive
)
