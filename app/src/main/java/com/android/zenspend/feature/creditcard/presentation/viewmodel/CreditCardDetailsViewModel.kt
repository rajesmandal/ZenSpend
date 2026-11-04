package com.android.zenspend.feature.creditcard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.android.zenspend.core.ui.util.formatMinorUnits
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.creditcard.domain.usecase.ObserveCreditCardDetailsUseCase
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardDetailsUiEffect
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardDetailsUiEvent
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardDetailsUiState
import com.android.zenspend.feature.emi.domain.usecase.RecordEmiInstallmentPaidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardDetailsViewModel @Inject constructor(
    private val observeCreditCardDetailsUseCase: ObserveCreditCardDetailsUseCase,
    private val recordEmiInstallmentPaidUseCase: RecordEmiInstallmentPaidUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditCardDetailsUiState())
    val uiState: StateFlow<CreditCardDetailsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreditCardDetailsUiEffect>()
    val uiEffect: Flow<CreditCardDetailsUiEffect> = _uiEffect.receiveAsFlow()

    private var observeJob: Job? = null

    fun handleEvent(event: CreditCardDetailsUiEvent) {
        when (event) {
            is CreditCardDetailsUiEvent.Load -> observeDetails(event.cardId)
            is CreditCardDetailsUiEvent.MarkEmiPaid -> markPaid(event.emiId)
        }
    }

    private fun observeDetails(cardId: Long) {
        observeJob?.cancel()
        observeJob = observeCreditCardDetailsUseCase(cardId)
            .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
            .onEach { details ->
                if (details == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Credit card not found") }
                } else {
                    val card = details.summary.card
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bankName = card.bankName,
                            cardName = card.name,
                            lastFourDigits = card.lastFourDigits ?: "****",
                            creditLimit = formatAmount(card.creditLimit),
                            outstanding = formatAmount(details.summary.outstanding),
                            availableLimit = formatAmount(details.summary.availableLimit),
                            utilization = details.summary.utilization.toFloat(),
                            activeEmis = details.activeEmis
                        )
                    }
                }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun markPaid(emiId: Long) {
        viewModelScope.launch {
            try {
                recordEmiInstallmentPaidUseCase(emiId)
                _uiEffect.send(CreditCardDetailsUiEffect.ShowSnackbar("Installment marked as paid"))
            } catch (e: Exception) {
                _uiEffect.send(CreditCardDetailsUiEffect.ShowSnackbar(e.message ?: "Could not update EMI"))
            }
        }
    }

    private fun formatAmount(amount: Long): String =
        formatMinorUnits(amount)
}
