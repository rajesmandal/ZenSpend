package com.android.zenspend.core.monetization.data.repository

import android.app.Activity
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MockBillingRepositoryImpl @Inject constructor() : BillingRepository {

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Free)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    override suspend fun startBillingConnection() {
        // No-op
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String) {
        // Simulate success
        _subscriptionState.value = SubscriptionState.Premium
    }

    override suspend fun restorePurchases() {
        // Simulate restore
        _subscriptionState.value = SubscriptionState.Premium
    }
}
