package com.android.zenspend.feature.creditcard.presentation.contract

import com.android.zenspend.feature.creditcard.presentation.model.CreditCardUiModel

data class CreditCardUiState(
    val isLoading: Boolean = false,
    val cards: List<CreditCardUiModel> = emptyList(),
    val error: String? = null,
    val formState: CreditCardFormState = CreditCardFormState()
)

data class CreditCardFormState(
    val cardName: String = "",
    val bankName: String = "",
    val lastFourDigits: String = "",
    val creditLimitText: String = "",
    val billingDay: String = "",
    val dueDay: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface CreditCardUiEvent {
    object LoadCreditCards : CreditCardUiEvent
    data class OnCardNameChange(val name: String) : CreditCardUiEvent
    data class OnBankNameChange(val bank: String) : CreditCardUiEvent
    data class OnLastFourChange(val digits: String) : CreditCardUiEvent
    data class OnLimitChange(val limit: String) : CreditCardUiEvent
    data class OnBillingDayChange(val day: String) : CreditCardUiEvent
    data class OnDueDayChange(val day: String) : CreditCardUiEvent
    object SaveCreditCard : CreditCardUiEvent
}

sealed interface CreditCardUiEffect {
    data class ShowSnackbar(val message: String) : CreditCardUiEffect
    object NavigateBack : CreditCardUiEffect
}
