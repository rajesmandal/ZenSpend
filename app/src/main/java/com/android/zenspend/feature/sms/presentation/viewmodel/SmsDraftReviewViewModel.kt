package com.android.zenspend.feature.sms.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.android.zenspend.core.ui.util.MoneyFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
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
    data class DestinationWalletSelected(val id: Long) : SmsDraftReviewEvent()
    data class TypeChanged(val type: TransactionType) : SmsDraftReviewEvent()
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
                val (walletId, destinationId) = matchAccounts(draft, draft.type, wallets, cards)

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
                    amount = MoneyFormat.rawFromMinor(draft.amount),
                    title = draft.merchant ?: "Transaction from ${draft.sender}",
                    type = draft.type,
                    selectedWalletId = walletId,
                    selectedDestinationWalletId = destinationId,
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
            is SmsDraftReviewEvent.DestinationWalletSelected -> _state.update { it.copy(selectedDestinationWalletId = event.id) }
            is SmsDraftReviewEvent.TypeChanged -> changeType(event.type)
            is SmsDraftReviewEvent.CategorySelected -> _state.update { it.copy(selectedCategoryId = event.id) }
            is SmsDraftReviewEvent.AmountChanged -> _state.update { it.copy(amount = event.amount.filter { c -> c.isDigit() || c == '.' }) }
            is SmsDraftReviewEvent.TitleChanged -> _state.update { it.copy(title = event.title) }
            SmsDraftReviewEvent.ConfirmClicked -> confirmDraft()
        }
    }

    private fun changeType(type: TransactionType) {
        _state.update { current ->
            val draft = current.draft ?: return@update current.copy(type = type)
            val (walletId, destinationId) = matchAccounts(draft, type, current.wallets, current.creditCards)
            current.copy(
                type = type,
                selectedWalletId = walletId,
                selectedDestinationWalletId = destinationId
            )
        }
    }

    /**
     * Picks the most likely wallet using the last 4 digits found in the SMS.
     *  - A registered credit card with those digits -> the card's wallet (card spend/refund/bill payment)
     *  - Otherwise a bank/cash/UPI wallet whose name contains the digits
     * For a credit card bill payment the card is the destination and the bank is the source.
     */
    private fun matchAccounts(
        draft: SmsTransactionDraft,
        type: TransactionType,
        wallets: List<Wallet>,
        cards: List<CreditCard>
    ): Pair<Long?, Long?> {
        val last4 = draft.accountLast4?.takeIf { it.isNotBlank() }
        val cardWalletId = last4?.let { digits ->
            cards.find { it.lastFourDigits == digits }?.walletId
                ?: wallets.find { it.type == WalletType.CREDIT_CARD && it.name.contains(digits) }?.id
        }
        val bankWalletId = last4?.let { digits ->
            wallets.find { it.type != WalletType.CREDIT_CARD && it.name.contains(digits, ignoreCase = true) }?.id
        }

        return when (type) {
            TransactionType.CREDIT_CARD_PAYMENT -> {
                // SMS is normally sent for the card; the paying bank account has to be chosen by the user
                val card = cardWalletId ?: wallets.singleOrNull { it.type == WalletType.CREDIT_CARD }?.id
                val bank = bankWalletId?.takeIf { it != card }
                bank to card
            }
            else -> (cardWalletId ?: bankWalletId) to null
        }
    }

    private fun confirmDraft() {
        val currentState = _state.value
        val draft = currentState.draft ?: return
        val selected = currentState.selectedWalletId
        val destination = currentState.selectedDestinationWalletId
        
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
                // EXPENSE: money leaves the selected wallet/card (subtracts)
                // INCOME / REFUND: money enters the selected wallet/card (adds)
                // CREDIT_CARD_PAYMENT: leaves the bank wallet, enters the card
                sourceWalletId = when (currentState.type) {
                    TransactionType.EXPENSE, TransactionType.CREDIT_CARD_PAYMENT, TransactionType.TRANSFER -> selected
                    else -> null
                },
                destinationWalletId = when (currentState.type) {
                    TransactionType.INCOME, TransactionType.REFUND -> selected
                    TransactionType.CREDIT_CARD_PAYMENT, TransactionType.TRANSFER -> destination
                    else -> null
                },
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
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = "Failed to add transaction: ${result.error}") }
                else -> {}
            }
        }
    }
}
