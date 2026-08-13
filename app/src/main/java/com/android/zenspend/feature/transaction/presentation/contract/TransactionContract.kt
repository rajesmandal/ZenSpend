package com.android.zenspend.feature.transaction.presentation.contract

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionUiModel> = emptyList(),
    val filter: TransactionFilterState = TransactionFilterState(),
    val wallets: List<com.android.zenspend.feature.wallet.presentation.model.WalletUiModel> = emptyList(),
    val categories: List<com.android.zenspend.feature.category.domain.model.Category> = emptyList(),
    val error: String? = null,
    val formState: TransactionFormState = TransactionFormState(),
    val categorySuggestion: com.android.zenspend.feature.category.domain.suggestion.CategorySuggestion? = null
)

data class TransactionFilterState(
    val query: String = "",
    val walletId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

data class TransactionFormState(
    val id: Long = 0,
    val amountText: String = "",
    val title: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: TransactionType = TransactionType.EXPENSE,
    val sourceWalletId: Long = 0,
    val destinationWalletId: Long? = null,
    val categoryId: Long? = null,
    val originalTransactionId: Long? = null,
    val isSubmitting: Boolean = false,
    val amountError: String? = null,
    val walletError: String? = null,
    val isEditMode: Boolean = false
)

sealed interface TransactionUiEvent {
    object LoadTransactions : TransactionUiEvent
    data class SearchChanged(val query: String) : TransactionUiEvent
    data class WalletFilterChanged(val walletId: Long?) : TransactionUiEvent
    data class CategoryFilterChanged(val categoryId: Long?) : TransactionUiEvent
    data class TypeFilterChanged(val type: TransactionType?) : TransactionUiEvent
    
    // Form Events
    data class OnTypeChange(val type: TransactionType) : TransactionUiEvent
    data class OnAmountChange(val amount: String) : TransactionUiEvent
    data class OnTitleChange(val title: String) : TransactionUiEvent
    data class OnNoteChange(val note: String) : TransactionUiEvent
    data class OnDateChange(val date: Long) : TransactionUiEvent
    data class OnSourceWalletChange(val id: Long) : TransactionUiEvent
    data class OnDestinationWalletChange(val id: Long?) : TransactionUiEvent
    data class OnCategoryChange(val id: Long?) : TransactionUiEvent
    
    object SaveTransaction : TransactionUiEvent
    data class DeleteTransaction(val id: Long) : TransactionUiEvent
    data class OnVoiceDraftAccepted(val draft: com.android.zenspend.feature.voice.domain.model.VoiceTransactionDraft) : TransactionUiEvent
}

sealed interface TransactionUiEffect {
    data class ShowSnackbar(val message: String) : TransactionUiEffect
    object NavigateBack : TransactionUiEffect
    object NavigateToAddTransaction : TransactionUiEffect
    data class NavigateToDetails(val id: Long) : TransactionUiEffect
    data class NavigateToEdit(val id: Long) : TransactionUiEffect
}
