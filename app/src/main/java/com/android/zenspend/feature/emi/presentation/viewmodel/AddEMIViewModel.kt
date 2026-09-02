package com.android.zenspend.feature.emi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.usecase.AddEMIUseCase
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEffect
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEvent
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddEMIViewModel @Inject constructor(
    private val addEMIUseCase: AddEMIUseCase,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEMIUiState())
    val uiState: StateFlow<AddEMIUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddEMIUiEffect>()
    val uiEffect: Flow<AddEMIUiEffect> = _uiEffect.receiveAsFlow()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun handleEvent(event: AddEMIUiEvent) {
        when (event) {
            is AddEMIUiEvent.Init -> init(event.creditCardId)
            is AddEMIUiEvent.OnNameChange -> _uiState.update { it.copy(name = event.name) }
            is AddEMIUiEvent.OnPrincipalChange -> _uiState.update { it.copy(principalText = event.amount) }
            is AddEMIUiEvent.OnEmiAmountChange -> _uiState.update { it.copy(emiAmountText = event.amount) }
            is AddEMIUiEvent.OnTenureChange -> _uiState.update { it.copy(tenureText = event.tenure) }
            is AddEMIUiEvent.OnStartDateChange -> _uiState.update {
                it.copy(
                    startDateMillis = event.millis,
                    startDateLabel = dateFormat.format(Date(event.millis))
                )
            }
            is AddEMIUiEvent.OnDueDayChange -> _uiState.update { it.copy(dueDayText = event.day) }
            is AddEMIUiEvent.OnInterestRateChange -> _uiState.update { it.copy(interestRateText = event.rate) }
            AddEMIUiEvent.Save -> save()
        }
    }

    private fun init(creditCardId: Long) {
        viewModelScope.launch {
            val card = creditCardRepository.getCreditCardById(creditCardId)
            val label = if (card != null) {
                val last4 = card.lastFourDigits?.let { " ••••$it" } ?: ""
                "${card.bankName}$last4"
            } else {
                "Unknown card"
            }
            val now = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    creditCardId = creditCardId,
                    linkedCardLabel = label,
                    startDateMillis = now,
                    startDateLabel = dateFormat.format(Date(now))
                )
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.creditCardId <= 0L) {
            _uiState.update { it.copy(error = "Invalid credit card") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                addEMIUseCase(
                    creditCardId = state.creditCardId,
                    name = state.name,
                    principalAmount = parseAmount(state.principalText),
                    emiAmount = parseAmount(state.emiAmountText),
                    totalTenure = state.tenureText.toIntOrNull()
                        ?: throw IllegalArgumentException("Enter valid tenure in months"),
                    startDate = state.startDateMillis,
                    dueDay = state.dueDayText.toIntOrNull()
                        ?: throw IllegalArgumentException("Enter valid due day"),
                    interestRate = state.interestRateText.toDoubleOrNull()
                )
                _uiEffect.send(AddEMIUiEffect.ShowSnackbar("EMI added successfully"))
                _uiEffect.send(AddEMIUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }

    private fun parseAmount(text: String): Long {
        val value = text.toDoubleOrNull()
            ?: throw IllegalArgumentException("Enter valid amount")
        if (value <= 0) throw IllegalArgumentException("Amount must be greater than zero")
        return (value * 100).toLong()
    }
}
