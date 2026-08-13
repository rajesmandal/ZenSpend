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
