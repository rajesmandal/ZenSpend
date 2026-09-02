package com.android.zenspend.core.monetization.domain.model

object BillingProduct {
    const val PREMIUM_MONTHLY = "zen_spend_premium_monthly"
    const val PREMIUM_YEARLY = "zen_spend_premium_yearly"
    
    val ALL_PRODUCTS = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY)
}
