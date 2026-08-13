package com.android.zenspend.feature.transaction.presentation.model

import com.android.zenspend.core.database.model.TransactionType

data class TransactionUiModel(
    val id: Long,
    val title: String,
    val amount: String, // Formatted
    val date: String,   // Formatted
    val type: TransactionType,
    val categoryName: String,
    val walletName: String,
    val note: String?
)
