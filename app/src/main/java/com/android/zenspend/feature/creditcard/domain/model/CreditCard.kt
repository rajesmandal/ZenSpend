package com.android.zenspend.feature.creditcard.domain.model

data class CreditCard(
    val id: Long = 0,
    val walletId: Long,
    val name: String,
    val bankName: String,
    val lastFourDigits: String?,
    val creditLimit: Long,
    val billingCycleDate: Int,
    val paymentDueDate: Int,
    val active: Boolean = true
)
