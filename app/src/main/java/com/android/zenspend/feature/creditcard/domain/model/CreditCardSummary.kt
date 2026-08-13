package com.android.zenspend.feature.creditcard.domain.model

data class CreditCardSummary(
    val card: CreditCard,
    val outstanding: Long,
    val availableLimit: Long,
    val utilization: Double
)
