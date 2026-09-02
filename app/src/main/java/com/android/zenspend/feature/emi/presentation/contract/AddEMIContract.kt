package com.android.zenspend.feature.emi.presentation.contract

data class AddEMIUiState(
    val creditCardId: Long = 0L,
    val linkedCardLabel: String = "",
    val name: String = "",
    val principalText: String = "",
    val emiAmountText: String = "",
    val tenureText: String = "",
    val startDateMillis: Long = System.currentTimeMillis(),
    val startDateLabel: String = "",
    val dueDayText: String = "1",
    val interestRateText: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface AddEMIUiEvent {
    data class Init(val creditCardId: Long) : AddEMIUiEvent
    data class OnNameChange(val name: String) : AddEMIUiEvent
    data class OnPrincipalChange(val amount: String) : AddEMIUiEvent
    data class OnEmiAmountChange(val amount: String) : AddEMIUiEvent
    data class OnTenureChange(val tenure: String) : AddEMIUiEvent
    data class OnStartDateChange(val millis: Long) : AddEMIUiEvent
    data class OnDueDayChange(val day: String) : AddEMIUiEvent
    data class OnInterestRateChange(val rate: String) : AddEMIUiEvent
    object Save : AddEMIUiEvent
}

sealed interface AddEMIUiEffect {
    data class ShowSnackbar(val message: String) : AddEMIUiEffect
    object NavigateBack : AddEMIUiEffect
}
