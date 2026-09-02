package com.android.zenspend.feature.creditcard.domain.model

data class CreditCardSummary(
    val card: CreditCard,
    val outstanding: Long,
    val availableLimit: Long,
    val utilization: Double,
    val activeEmiCount: Int = 0
)

data class CreditCardDetails(
    val summary: CreditCardSummary,
    val activeEmis: List<com.android.zenspend.feature.emi.domain.model.EMI>
)
