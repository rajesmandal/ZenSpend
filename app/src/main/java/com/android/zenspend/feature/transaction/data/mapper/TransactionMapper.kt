package com.android.zenspend.feature.transaction.data.mapper

import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.feature.transaction.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = date,
        note = note,
        type = type,
        status = status,
        source = source,
        sourceWalletId = sourceWalletId,
        destinationWalletId = destinationWalletId,
        categoryId = categoryId,
        emiId = emiId,
        recurringTransactionId = recurringTransactionId,
        recurringOccurrenceKey = recurringOccurrenceKey,
        originalTransactionId = originalTransactionId,
        externalReference = externalReference,
        isAutoDetected = isAutoDetected,
        isUserConfirmed = isUserConfirmed,
        isExcludedFromReports = isExcludedFromReports
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        date = date,
        note = note,
        type = type,
        status = status,
        source = source,
        sourceWalletId = sourceWalletId,
        destinationWalletId = destinationWalletId,
        categoryId = categoryId,
        emiId = emiId,
        recurringTransactionId = recurringTransactionId,
        recurringOccurrenceKey = recurringOccurrenceKey,
        originalTransactionId = originalTransactionId,
        externalReference = externalReference,
        isAutoDetected = isAutoDetected,
        isUserConfirmed = isUserConfirmed,
        isExcludedFromReports = isExcludedFromReports
    )
}
