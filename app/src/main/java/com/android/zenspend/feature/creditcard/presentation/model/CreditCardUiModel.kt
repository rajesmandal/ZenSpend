package com.android.zenspend.feature.creditcard.presentation.model

data class CreditCardUiModel(
    val id: Long,
    val walletId: Long,
    val cardName: String,
    val bankName: String,
    val lastFourDigits: String,
    val creditLimit: String,
    val outstanding: String,
    val availableLimit: String,
    val utilization: Float,
    val billingDate: String,
    val dueDate: String,
    val rawOutstanding: Long,
    val activeEmiCount: Int = 0
)
