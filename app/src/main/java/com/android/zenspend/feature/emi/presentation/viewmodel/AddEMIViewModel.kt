package com.android.zenspend.feature.emi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.ui.util.MoneyFormat
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.model.EmiSchedule
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.emi.domain.usecase.EmiDraft
import com.android.zenspend.feature.emi.domain.usecase.SaveEMIUseCase
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEffect
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEvent
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiState
import com.android.zenspend.feature.emi.presentation.contract.CardOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddEMIViewModel @Inject constructor(
    private val saveEMIUseCase: SaveEMIUseCase,
    private val emiRepository: EMIRepository,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEMIUiState())
    val uiState: StateFlow<AddEMIUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddEMIUiEffect>()
    val uiEffect: Flow<AddEMIUiEffect> = _uiEffect.receiveAsFlow()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var initialised = false

    fun handleEvent(event: AddEMIUiEvent) {
        when (event) {
            is AddEMIUiEvent.Init -> init(event.creditCardId, event.emiId)
            is AddEMIUiEvent.OnLenderTypeChange -> _uiState.update { it.copy(lenderType = event.type, error = null) }
            is AddEMIUiEvent.OnLenderNameChange -> _uiState.update { it.copy(lenderName = event.name) }
            is AddEMIUiEvent.OnCardSelected -> _uiState.update { it.copy(selectedCardId = event.cardId) }
            is AddEMIUiEvent.OnNameChange -> _uiState.update { it.copy(name = event.name) }
            is AddEMIUiEvent.OnPrincipalChange -> _uiState.update { it.copy(principalText = event.amount) }
            is AddEMIUiEvent.OnEmiAmountChange -> _uiState.update { it.copy(emiAmountText = event.amount) }
            is AddEMIUiEvent.OnTenureChange -> _uiState.update { it.copy(tenureText = event.tenure.filter(Char::isDigit)) }
            is AddEMIUiEvent.OnPaidChange -> _uiState.update { it.copy(paidText = event.paid.filter(Char::isDigit)) }
            is AddEMIUiEvent.OnStartDateChange -> _uiState.update {
                it.copy(startDateMillis = event.millis, startDateLabel = dateFormat.format(Date(event.millis)))
            }
            is AddEMIUiEvent.OnDueDayChange -> _uiState.update { it.copy(dueDayText = event.day.filter(Char::isDigit)) }
            is AddEMIUiEvent.OnInterestRateChange -> _uiState.update { it.copy(interestRateText = event.rate) }
            is AddEMIUiEvent.OnNoteChange -> _uiState.update { it.copy(noteText = event.note) }
            AddEMIUiEvent.Save -> save()
        }
    }

    private fun init(creditCardId: Long, emiId: Long) {
        if (initialised) return
        initialised = true
        viewModelScope.launch {
            val cards = creditCardRepository.getAllActiveCreditCards().first().map { card ->
                val last4 = card.lastFourDigits?.let { " ••••$it" }.orEmpty()
                CardOption(card.id, "${card.bankName}$last4")
            }
            val now = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    cards = cards,
                    selectedCardId = creditCardId.takeIf { id -> id > 0 } ?: cards.singleOrNull()?.id,
                    lenderType = if (creditCardId > 0) EMILenderType.CREDIT_CARD else EMILenderType.BANK,
                    startDateMillis = now,
                    startDateLabel = dateFormat.format(Date(now))
                )
            }
            if (emiId > 0) loadForEdit(emiId, cards)
        }
    }

    private suspend fun loadForEdit(emiId: Long, cards: List<CardOption>) {
        val emi = emiRepository.getEMIById(emiId) ?: return
        val cardId = emi.walletId?.let { walletId ->
            creditCardRepository.getAllActiveCreditCards().first().firstOrNull { it.walletId == walletId }?.id
        }
        _uiState.update {
            it.copy(
                emiId = emi.id,
                lenderType = emi.lenderType,
                lenderName = if (emi.lenderType == EMILenderType.CREDIT_CARD) "" else emi.lenderName,
                selectedCardId = cardId ?: it.selectedCardId,
                cards = cards,
                name = emi.name,
                principalText = MoneyFormat.rawFromMinor(emi.principalAmount),
                emiAmountText = MoneyFormat.rawFromMinor(emi.emiAmount),
                tenureText = emi.totalTenure.toString(),
                paidText = EmiSchedule.paidInstallments(emi).toString(),
                startDateMillis = emi.startDate,
                startDateLabel = dateFormat.format(Date(emi.startDate)),
                dueDayText = emi.dueDay.toString(),
                interestRateText = emi.interestRate?.toString().orEmpty(),
                noteText = emi.note.orEmpty()
            )
        }
    }

    private fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                saveEMIUseCase(
                    draft = EmiDraft(
                        lenderType = state.lenderType,
                        lenderName = state.lenderName,
                        creditCardId = state.selectedCardId,
                        name = state.name,
                        principalAmount = parseAmount(state.principalText),
                        emiAmount = parseAmount(state.emiAmountText),
                        totalTenure = state.tenureText.toIntOrNull()
                            ?: throw IllegalArgumentException("Enter the tenure in months"),
                        paidInstallments = state.paidText.toIntOrNull() ?: 0,
                        startDate = state.startDateMillis,
                        dueDay = state.dueDayText.toIntOrNull()
                            ?: throw IllegalArgumentException("Enter a due day between 1 and 31"),
                        interestRate = state.interestRateText.toDoubleOrNull(),
                        note = state.noteText
                    ),
                    existingId = state.emiId
                )
                _uiEffect.send(AddEMIUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }

    private fun parseAmount(text: String): Long {
        val value = text.toDoubleOrNull() ?: throw IllegalArgumentException("Enter a valid amount")
        if (value <= 0) throw IllegalArgumentException("Amount must be greater than zero")
        return Math.round(value * MINOR_PER_MAJOR)
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100
    }
}
