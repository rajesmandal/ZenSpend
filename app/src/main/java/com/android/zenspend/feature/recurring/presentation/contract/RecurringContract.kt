package com.android.zenspend.feature.recurring.presentation.contract

import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction

data class RecurringUiState(
    val isLoading: Boolean = false,
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val error: String? = null,
    val formState: RecurringFormState = RecurringFormState()
)

data class RecurringFormState(
    val id: Long = 0,
    val name: String = "",
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val frequency: RecurrenceType = RecurrenceType.MONTHLY,
    val sourceWalletId: Long? = null,
    val destinationWalletId: Long? = null,
    val categoryId: Long? = null,
    val startDate: Long = System.currentTimeMillis(),
    val nextOccurrenceDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val isActive: Boolean = true,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false
)

sealed interface RecurringUiEvent {
    object LoadRecurring : RecurringUiEvent
    data class ToggleActive(val id: Long, val current: Boolean) : RecurringUiEvent
    data class Archive(val id: Long) : RecurringUiEvent
    data class ConfirmOccurrence(val id: Long, val date: Long) : RecurringUiEvent
    
    // Form events
    data class OnNameChange(val name: String) : RecurringUiEvent
    data class OnAmountChange(val amount: String) : RecurringUiEvent
    data class OnTypeChange(val type: TransactionType) : RecurringUiEvent
    data class OnFrequencyChange(val freq: RecurrenceType) : RecurringUiEvent
    data class OnSourceWalletChange(val id: Long?) : RecurringUiEvent
    data class OnDestinationWalletChange(val id: Long?) : RecurringUiEvent
    data class OnCategoryChange(val id: Long?) : RecurringUiEvent
    data class OnStartDateChange(val date: Long) : RecurringUiEvent
    
    object SaveRecurring : RecurringUiEvent
}

sealed interface RecurringUiEffect {
    data class ShowSnackbar(val message: String) : RecurringUiEffect
    object NavigateBack : RecurringUiEffect
}
