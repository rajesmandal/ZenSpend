package com.android.zenspend.feature.dashboard.presentation.model

data class TransactionUi(
    val id: Long,
    val title: String,
    val amount: String, // Formatted amount for display
    val category: String,
    val date: String,   // Formatted date for display
    val note: String?
)
