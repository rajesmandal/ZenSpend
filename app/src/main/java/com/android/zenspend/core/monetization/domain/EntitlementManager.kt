package com.android.zenspend.core.monetization.domain

import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import com.android.zenspend.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntitlementManager @Inject constructor(
    private val billingRepository: BillingRepository
) {
    val subscriptionState: Flow<SubscriptionState> = billingRepository.subscriptionState

    /**
     * Whether Premium features are available. While [BuildConfig.PREMIUM_GATES_ENABLED] is false
     * (testing phase) every feature is open and ads are off.
     */
    val isPremium: Flow<Boolean> = if (BuildConfig.PREMIUM_GATES_ENABLED) {
        subscriptionState.map { it is SubscriptionState.Premium }
    } else {
        flowOf(true)
    }

    suspend fun restorePurchases() {
        billingRepository.restorePurchases()
    }
}
