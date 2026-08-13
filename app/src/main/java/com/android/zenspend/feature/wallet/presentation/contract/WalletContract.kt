package com.android.zenspend.feature.wallet.presentation.contract

import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.wallet.presentation.model.WalletUiModel

data class WalletUiState(
    val isLoading: Boolean = false,
    val wallets: List<WalletUiModel> = emptyList(),
    val totalBalance: String = "0.00",
    val assetBalance: String = "0.00",
    val liabilityBalance: String = "0.00",
    val error: String? = null,
    
    // Form State (for Add/Edit)
    val formState: WalletFormState = WalletFormState(),
    
    // Details State
    val detailsState: WalletDetailsState = WalletDetailState()
)

data class WalletFormState(
    val id: Long = 0,
    val name: String = "",
    val type: WalletType = WalletType.CASH,
    val openingBalanceText: String = "",
    val currency: String = "INR",
    val icon: String? = null,
    val color: Int? = null,
    val isSubmitting: Boolean = false,
    val nameError: String? = null,
    val balanceError: String? = null,
    val isEditMode: Boolean = false
)

sealed interface WalletDetailsState {
    object Idle : WalletDetailsState
    object Loading : WalletDetailsState
    data class Success(
        val wallet: WalletUiModel,
        val recentTransactions: List<com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel> = emptyList()
    ) : WalletDetailsState
    data class Error(val message: String) : WalletDetailsState
}

// Just to avoid naming confusion
private fun WalletDetailState() = WalletDetailsState.Idle

sealed interface WalletUiEvent {
    object LoadWallets : WalletUiEvent
    data class LoadWalletDetails(val id: Long) : WalletUiEvent
    data class LoadWalletForEdit(val id: Long) : WalletUiEvent
    
    // Form Events
    data class OnNameChange(val name: String) : WalletUiEvent
    data class OnTypeChange(val type: WalletType) : WalletUiEvent
    data class OnBalanceChange(val balance: String) : WalletUiEvent
    object SaveWallet : WalletUiEvent
    
    data class ArchiveWallet(val id: Long) : WalletUiEvent
}

sealed interface WalletUiEffect {
    data class ShowSnackbar(val message: String) : WalletUiEffect
    object NavigateBack : WalletUiEffect
    data class NavigateToDetails(val id: Long) : WalletUiEffect
    data class NavigateToEdit(val id: Long) : WalletUiEffect
}
