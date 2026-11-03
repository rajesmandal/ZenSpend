package com.android.zenspend.feature.transaction.domain.model

import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Long, // in minor units
    val date: Long,
    val note: String?,
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val source: TransactionSource = TransactionSource.MANUAL,
    
    val sourceWalletId: Long?,
    val destinationWalletId: Long? = null,
    val categoryId: Long? = null,
    val emiId: Long? = null,
    val recurringTransactionId: Long? = null,
    val recurringOccurrenceKey: String? = null,
    val originalTransactionId: Long? = null,
    
    val externalReference: String? = null,
    val isAutoDetected: Boolean = false,
    val isUserConfirmed: Boolean = true,
    val isExcludedFromReports: Boolean = false
)

/**
 * Keeps only the wallet legs that make sense for the transaction type, so wallet balances
 * (opening + incoming(destination) - outgoing(source)) are always added/subtracted correctly.
 *  - EXPENSE: money leaves source wallet only
 *  - INCOME / REFUND: money enters destination wallet only
 *  - TRANSFER / CREDIT_CARD_PAYMENT: leaves source, enters destination
 */
fun Transaction.withNormalizedWallets(): Transaction {
    val source = sourceWalletId?.takeIf { it != 0L }
    val destination = destinationWalletId?.takeIf { it != 0L }
    return when (type) {
        TransactionType.EXPENSE -> copy(sourceWalletId = source, destinationWalletId = null)
        TransactionType.INCOME, TransactionType.REFUND -> copy(sourceWalletId = null, destinationWalletId = destination)
        TransactionType.TRANSFER, TransactionType.CREDIT_CARD_PAYMENT ->
            copy(sourceWalletId = source, destinationWalletId = destination)
    }
}
