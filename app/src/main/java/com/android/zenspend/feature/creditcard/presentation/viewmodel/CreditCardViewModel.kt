package com.android.zenspend.feature.creditcard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.creditcard.domain.model.CreditCardSummary
import com.android.zenspend.feature.creditcard.domain.usecase.AddCreditCardUseCase
import com.android.zenspend.feature.creditcard.domain.usecase.ObserveCreditCardsUseCase
import com.android.zenspend.feature.creditcard.presentation.contract.*
import com.android.zenspend.feature.creditcard.presentation.model.CreditCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val observeCreditCardsUseCase: ObserveCreditCardsUseCase,
    private val addCreditCardUseCase: AddCreditCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditCardUiState())
    val uiState: StateFlow<CreditCardUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreditCardUiEffect>()
    val uiEffect: Flow<CreditCardUiEffect> = _uiEffect.receiveAsFlow()

    init {
        handleEvent(CreditCardUiEvent.LoadCreditCards)
    }

    fun handleEvent(event: CreditCardUiEvent) {
        when (event) {
            is CreditCardUiEvent.LoadCreditCards -> observeCards()
            is CreditCardUiEvent.OnCardNameChange -> _uiState.update { it.copy(formState = it.formState.copy(cardName = event.name)) }
            is CreditCardUiEvent.OnBankNameChange -> _uiState.update { it.copy(formState = it.formState.copy(bankName = event.bank)) }
            is CreditCardUiEvent.OnLastFourChange -> _uiState.update { it.copy(formState = it.formState.copy(lastFourDigits = event.digits)) }
            is CreditCardUiEvent.OnLimitChange -> _uiState.update { it.copy(formState = it.formState.copy(creditLimitText = event.limit)) }
            is CreditCardUiEvent.OnBillingDayChange -> _uiState.update { it.copy(formState = it.formState.copy(billingDay = event.day)) }
            is CreditCardUiEvent.OnDueDayChange -> _uiState.update { it.copy(formState = it.formState.copy(dueDay = event.day)) }
            is CreditCardUiEvent.SaveCreditCard -> saveCreditCard()
        }
    }

    private fun observeCards() {
        observeCreditCardsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { cards ->
                _uiState.update { it.copy(isLoading = false, cards = cards.map { it.toUi() }) }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun saveCreditCard() {
        val form = _uiState.value.formState
        if (form.cardName.isBlank() || form.bankName.isBlank()) {
            _uiState.update { it.copy(formState = it.formState.copy(error = "Required fields missing")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = true)) }
            try {
                addCreditCardUseCase(
                    name = form.cardName,
                    bankName = form.bankName,
                    lastFourDigits = form.lastFourDigits.takeIf { it.isNotBlank() },
                    creditLimit = form.creditLimitText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L,
                    billingCycleDate = form.billingDay.toIntOrNull() ?: 1,
                    paymentDueDate = form.dueDay.toIntOrNull() ?: 15
                )
                _uiEffect.send(CreditCardUiEffect.ShowSnackbar("Card added successfully"))
                _uiEffect.send(CreditCardUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = false, error = e.message)) }
            }
        }
    }

    private fun CreditCardSummary.toUi() = CreditCardUiModel(
        id = card.id,
        walletId = card.walletId,
        cardName = card.name,
        bankName = card.bankName,
        lastFourDigits = card.lastFourDigits ?: "****",
        creditLimit = formatAmount(card.creditLimit),
        outstanding = formatAmount(outstanding),
        availableLimit = formatAmount(availableLimit),
        utilization = utilization.toFloat(),
        billingDate = card.billingCycleDate.toString(),
        dueDate = card.paymentDueDate.toString(),
        rawOutstanding = outstanding
    )

    private fun formatAmount(amount: Long): String {
        return "%.2f".format(amount.toDouble() / 100.0)
    }
}
