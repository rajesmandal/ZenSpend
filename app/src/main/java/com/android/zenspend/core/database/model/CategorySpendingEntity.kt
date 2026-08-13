package com.android.zenspend.core.database.model

data class CategorySpendingEntity(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Long,
    val maxAmount: Long = 0,
    val transactionCount: Int = 0
)
