package com.android.zenspend.feature.recurring.data.mapper

import com.android.zenspend.core.database.entity.RecurringTransactionEntity
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction

fun RecurringTransactionEntity.toDomain(): RecurringTransaction {
    return RecurringTransaction(
        id = id,
        name = name,
        amount = amount,
        type = type,
        recurrenceType = recurrenceType,
        sourceWalletId = sourceWalletId,
        destinationWalletId = destinationWalletId,
        categoryId = categoryId,
        startDate = startDate,
        lastExecutedDate = lastExecutedDate,
        nextExecutionDate = nextExecutionDate,
        isActive = isActive,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RecurringTransaction.toEntity(): RecurringTransactionEntity {
    return RecurringTransactionEntity(
        id = id,
        name = name,
        amount = amount,
        type = type,
        recurrenceType = recurrenceType,
        sourceWalletId = sourceWalletId,
        destinationWalletId = destinationWalletId,
        categoryId = categoryId,
        startDate = startDate,
        lastExecutedDate = lastExecutedDate,
        nextExecutionDate = nextExecutionDate,
        isActive = isActive,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
