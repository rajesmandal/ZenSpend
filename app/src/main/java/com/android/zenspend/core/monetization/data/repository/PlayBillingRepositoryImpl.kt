package com.android.zenspend.core.monetization.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.zenspend.core.monetization.domain.model.BillingProduct
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Unknown)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        scope.launch {
            startBillingConnection()
        }
    }

    override suspend fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryPurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry logic if needed
            }
        })
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String) {
        // Query product details first, then launch flow
        // Simplified for this implementation
    }

    override suspend fun restorePurchases() {
        queryPurchases()
    }

    private suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchases = purchases.filter { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    it.products.any { p -> p in BillingProduct.ALL_PRODUCTS }
                }
                
                val hasPremium = activePurchases.isNotEmpty()
                _subscriptionState.value = if (hasPremium) SubscriptionState.Premium else SubscriptionState.Free
                
                // Acknowledge if not already done
                activePurchases.forEach { purchase ->
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase.purchaseToken)
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                // Log or handle acknowledgment error
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                queryPurchases()
            }
        }
    }
}
