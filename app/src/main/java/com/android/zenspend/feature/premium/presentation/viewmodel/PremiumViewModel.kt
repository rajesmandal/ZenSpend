package com.android.zenspend.feature.premium.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.monetization.domain.EntitlementManager
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val subscriptionState: SubscriptionState = SubscriptionState.Unknown,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val entitlementManager: EntitlementManager
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumUiState())
    val state: StateFlow<PremiumUiState> = _state.asStateFlow()

    init {
        observeSubscriptionState()
    }

    private fun observeSubscriptionState() {
        entitlementManager.subscriptionState
            .onEach { state -> _state.update { it.copy(subscriptionState = state) } }
            .launchIn(viewModelScope)
    }

    fun purchaseProduct(activity: Activity, productId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                billingRepository.launchPurchaseFlow(activity, productId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                entitlementManager.restorePurchases()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
