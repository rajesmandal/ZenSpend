package com.android.zenspend.feature.sms.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.domain.model.Category
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import com.android.zenspend.feature.sms.domain.usecase.ConfirmSmsDraftUseCase
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsDraftReviewState(
    val draft: SmsTransactionDraft? = null,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    
    val selectedWalletId: Long? = null,
    val selectedDestinationWalletId: Long? = null,
    val selectedCategoryId: Long? = null,
    val selectedCreditCardId: Long? = null,
    
    val amount: String = "",
    val title: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val categorySuggestion: com.android.zenspend.feature.category.domain.suggestion.CategorySuggestion? = null
)

sealed class SmsDraftReviewEvent {
    data class WalletSelected(val id: Long) : SmsDraftReviewEvent()
    data class CategorySelected(val id: Long) : SmsDraftReviewEvent()
    data class AmountChanged(val amount: String) : SmsDraftReviewEvent()
    data class TitleChanged(val title: String) : SmsDraftReviewEvent()
    object ConfirmClicked : SmsDraftReviewEvent()
}

@HiltViewModel
class SmsDraftReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val smsDraftRepository: SmsDraftRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val creditCardRepository: CreditCardRepository,
    private val confirmSmsDraftUseCase: ConfirmSmsDraftUseCase,
    private val getCategorySuggestionUseCase: com.android.zenspend.feature.category.domain.usecase.GetCategorySuggestionUseCase,
    private val updateMerchantPreferenceUseCase: com.android.zenspend.feature.category.domain.usecase.UpdateMerchantPreferenceUseCase
) : ViewModel() {

    private val draftId: Long = savedStateHandle.get<String>("draftId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(SmsDraftReviewState())
    val state: StateFlow<SmsDraftReviewState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val draft = smsDraftRepository.getPendingDrafts().firstOrNull()?.find { it.id == draftId }
            val wallets = walletRepository.getAllActiveWallets().first()
            val categories = categoryRepository.getAllActiveCategories().first()
            val cards = creditCardRepository.getAllActiveCreditCards().first()
            
            if (draft != null) {
                val matchedWallet = wallets.find { it.name.contains(draft.accountLast4 ?: "", ignoreCase = true) }
                val matchedCard = cards.find { it.lastFourDigits == draft.accountLast4 }
                
                val suggestion = getCategorySuggestionUseCase(
                    merchant = draft.merchant,
                    title = draft.merchant,
                    type = draft.type
                )
                
                _state.update { it.copy(
                    draft = draft,
                    wallets = wallets,
                    categories = categories,
                    creditCards = cards,
                    amount = (draft.amount / 100.0).toString(),
                    title = draft.merchant ?: "Transaction from ${draft.sender}",
                    type = draft.type,
                    selectedWalletId = matchedWallet?.id,
                    selectedCreditCardId = matchedCard?.id,
                    selectedCategoryId = suggestion?.categoryId,
                    categorySuggestion = suggestion,
                    isLoading = false
                ) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Draft not found") }
            }
        }
    }

    fun onEvent(event: SmsDraftReviewEvent) {
        when (event) {
            is SmsDraftReviewEvent.WalletSelected -> _state.update { it.copy(selectedWalletId = event.id) }
            is SmsDraftReviewEvent.CategorySelected -> _state.update { it.copy(selectedCategoryId = event.id) }
            is SmsDraftReviewEvent.AmountChanged -> _state.update { it.copy(amount = event.amount) }
            is SmsDraftReviewEvent.TitleChanged -> _state.update { it.copy(title = event.title) }
            SmsDraftReviewEvent.ConfirmClicked -> confirmDraft()
        }
    }

    private fun confirmDraft() {
        val currentState = _state.value
        val draft = currentState.draft ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val transaction = Transaction(
                title = currentState.title,
                amount = (currentState.amount.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() },
                date = draft.date,
                note = "Detected from SMS: ${draft.sender}",
                type = currentState.type,
                status = TransactionStatus.COMPLETED,
                source = TransactionSource.SMS,
                sourceWalletId = currentState.selectedWalletId,
                destinationWalletId = currentState.selectedDestinationWalletId,
                categoryId = currentState.selectedCategoryId,
                isAutoDetected = true
            )
            
            val result = confirmSmsDraftUseCase(draftId, transaction)
            when (result) {
                is Resource.Success -> {
                    updateMerchantPreferenceUseCase(
                        merchant = currentState.title,
                        title = currentState.title,
                        categoryId = currentState.selectedCategoryId
                    )
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = "Failed to add transaction") }
                else -> {}
            }
        }
    }
}
