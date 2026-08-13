package com.android.zenspend.feature.recurring.domain.model

import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType

data class RecurringTransaction(
    val id: Long = 0,
    val name: String,
    val amount: Long,
    val type: TransactionType,
    val recurrenceType: RecurrenceType,
    val sourceWalletId: Long?,
    val destinationWalletId: Long?,
    val categoryId: Long?,
    val startDate: Long,
    val lastExecutedDate: Long?,
    val nextExecutionDate: Long,
    val isActive: Boolean = true,
    val note: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = isActive && nextExecutionDate < System.currentTimeMillis()
}
