package com.android.zenspend.feature.transaction.domain.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val note: String?
)
