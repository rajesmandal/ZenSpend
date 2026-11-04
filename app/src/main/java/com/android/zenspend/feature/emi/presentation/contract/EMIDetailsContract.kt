package com.android.zenspend.feature.emi.presentation.contract

import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EMIPayment

/** A wallet the installment can be paid from. */
data class PayWalletOption(val id: Long, val name: String)

data class EMIDetailsUiState(
    val isLoading: Boolean = true,
    val emi: EMI? = null,
    val payments: List<EMIPayment> = emptyList(),
    val wallets: List<PayWalletOption> = emptyList()
)

sealed interface EMIDetailsUiEvent {
    /** [walletId] null = just mark as paid, without an expense. */
    data class MarkPaid(val walletId: Long?) : EMIDetailsUiEvent
    object UndoLast : EMIDetailsUiEvent
    object Close : EMIDetailsUiEvent
    object Delete : EMIDetailsUiEvent
}

sealed interface EMIDetailsUiEffect {
    data class ShowMessage(val messageRes: Int) : EMIDetailsUiEffect
    object NavigateBack : EMIDetailsUiEffect
}
