package com.android.zenspend.feature.creditcard.presentation.contract

import com.android.zenspend.feature.emi.domain.model.EMI

data class CreditCardDetailsUiState(
    val isLoading: Boolean = true,
    val bankName: String = "",
    val cardName: String = "",
    val lastFourDigits: String = "",
    val creditLimit: String = "",
    val outstanding: String = "",
    val availableLimit: String = "",
    val utilization: Float = 0f,
    val activeEmis: List<EMI> = emptyList(),
    val error: String? = null
)

sealed interface CreditCardDetailsUiEvent {
    data class Load(val cardId: Long) : CreditCardDetailsUiEvent
    data class MarkEmiPaid(val emiId: Long) : CreditCardDetailsUiEvent
}

sealed interface CreditCardDetailsUiEffect {
    data class ShowSnackbar(val message: String) : CreditCardDetailsUiEffect
}
