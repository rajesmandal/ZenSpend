package com.android.zenspend.feature.transaction.data.mapper

import com.android.zenspend.core.database.entity.ExpenseEntity
import com.android.zenspend.feature.transaction.domain.model.Transaction

fun ExpenseEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        category = category,
        date = date,
        note = note
    )
}

fun Transaction.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        title = title,
        amount = amount,
        category = category,
        date = date,
        note = note
    )
}
