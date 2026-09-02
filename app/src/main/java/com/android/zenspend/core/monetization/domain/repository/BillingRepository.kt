package com.android.zenspend.core.monetization.domain.repository

import android.app.Activity
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val subscriptionState: StateFlow<SubscriptionState>
    
    suspend fun startBillingConnection()
    suspend fun launchPurchaseFlow(activity: Activity, productId: String)
    suspend fun restorePurchases()
}
