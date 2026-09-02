package com.android.zenspend.core.monetization.domain

import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntitlementManager @Inject constructor(
    private val billingRepository: BillingRepository
) {
    val subscriptionState: Flow<SubscriptionState> = billingRepository.subscriptionState

    val isPremium: Flow<Boolean> = subscriptionState.map { it is SubscriptionState.Premium }

    suspend fun restorePurchases() {
        billingRepository.restorePurchases()
    }
}
