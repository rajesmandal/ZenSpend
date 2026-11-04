package com.android.zenspend.feature.emi.presentation.contract

import com.android.zenspend.core.database.model.EMILenderType

/** A credit card the EMI can be linked to. */
data class CardOption(val id: Long, val label: String)

data class AddEMIUiState(
    /** Set when editing an existing EMI. */
    val emiId: Long? = null,
    val isLoading: Boolean = false,
    val lenderType: EMILenderType = EMILenderType.BANK,
    val lenderName: String = "",
    val cards: List<CardOption> = emptyList(),
    val selectedCardId: Long? = null,
    val name: String = "",
    val principalText: String = "",
    val emiAmountText: String = "",
    val tenureText: String = "",
    val paidText: String = "",
    val startDateMillis: Long = System.currentTimeMillis(),
    val startDateLabel: String = "",
    val dueDayText: String = "1",
    val interestRateText: String = "",
    val noteText: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val isEdit: Boolean get() = emiId != null
}

sealed interface AddEMIUiEvent {
    /** [creditCardId] pre-selects a card (0 = none); [emiId] loads an EMI for editing (0 = new). */
    data class Init(val creditCardId: Long, val emiId: Long) : AddEMIUiEvent
    data class OnLenderTypeChange(val type: EMILenderType) : AddEMIUiEvent
    data class OnLenderNameChange(val name: String) : AddEMIUiEvent
    data class OnCardSelected(val cardId: Long) : AddEMIUiEvent
    data class OnNameChange(val name: String) : AddEMIUiEvent
    data class OnPrincipalChange(val amount: String) : AddEMIUiEvent
    data class OnEmiAmountChange(val amount: String) : AddEMIUiEvent
    data class OnTenureChange(val tenure: String) : AddEMIUiEvent
    data class OnPaidChange(val paid: String) : AddEMIUiEvent
    data class OnStartDateChange(val millis: Long) : AddEMIUiEvent
    data class OnDueDayChange(val day: String) : AddEMIUiEvent
    data class OnInterestRateChange(val rate: String) : AddEMIUiEvent
    data class OnNoteChange(val note: String) : AddEMIUiEvent
    object Save : AddEMIUiEvent
}

sealed interface AddEMIUiEffect {
    data class ShowSnackbar(val message: String) : AddEMIUiEffect
    object NavigateBack : AddEMIUiEffect
}
