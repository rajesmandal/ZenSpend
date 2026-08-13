package com.android.zenspend.feature.recurring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.usecase.*
import com.android.zenspend.feature.recurring.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val observeRecurringTransactionsUseCase: ObserveRecurringTransactionsUseCase,
    private val addRecurringTransactionUseCase: AddRecurringTransactionUseCase,
    private val updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase,
    private val setRecurringStatusUseCase: SetRecurringStatusUseCase,
    private val confirmRecurringOccurrenceUseCase: ConfirmRecurringOccurrenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<RecurringUiEffect>()
    val uiEffect: Flow<RecurringUiEffect> = _uiEffect.receiveAsFlow()

    init {
        handleEvent(RecurringUiEvent.LoadRecurring)
    }

    fun handleEvent(event: RecurringUiEvent) {
        when (event) {
            is RecurringUiEvent.LoadRecurring -> observeRecurring()
            is RecurringUiEvent.ToggleActive -> toggleStatus(event.id, event.current)
            is RecurringUiEvent.ConfirmOccurrence -> confirmOccurrence(event.id, event.date)
            
            is RecurringUiEvent.OnNameChange -> _uiState.update { it.copy(formState = it.formState.copy(name = event.name)) }
            is RecurringUiEvent.OnAmountChange -> _uiState.update { it.copy(formState = it.formState.copy(amountText = event.amount)) }
            is RecurringUiEvent.OnTypeChange -> _uiState.update { it.copy(formState = it.formState.copy(type = event.type)) }
            is RecurringUiEvent.OnFrequencyChange -> _uiState.update { it.copy(formState = it.formState.copy(frequency = event.freq)) }
            is RecurringUiEvent.OnSourceWalletChange -> _uiState.update { it.copy(formState = it.formState.copy(sourceWalletId = event.id)) }
            is RecurringUiEvent.OnDestinationWalletChange -> _uiState.update { it.copy(formState = it.formState.copy(destinationWalletId = event.id)) }
            is RecurringUiEvent.OnCategoryChange -> _uiState.update { it.copy(formState = it.formState.copy(categoryId = event.id)) }
            is RecurringUiEvent.OnStartDateChange -> _uiState.update { it.copy(formState = it.formState.copy(startDate = event.date, nextOccurrenceDate = event.date)) }
            
            is RecurringUiEvent.SaveRecurring -> saveRecurring()
        }
    }

    private fun observeRecurring() {
        observeRecurringTransactionsUseCase(includeInactive = true)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { list -> _uiState.update { it.copy(isLoading = false, recurringTransactions = list) } }
            .catch { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            .launchIn(viewModelScope)
    }

    private fun toggleStatus(id: Long, current: Boolean) {
        viewModelScope.launch {
            setRecurringStatusUseCase(id, !current)
        }
    }

    private fun confirmOccurrence(id: Long, date: Long) {
        viewModelScope.launch {
            val result = confirmRecurringOccurrenceUseCase(id, date)
            if (result is Resource.Error) {
                _uiEffect.send(RecurringUiEffect.ShowSnackbar("Error: ${result.error}"))
            } else {
                _uiEffect.send(RecurringUiEffect.ShowSnackbar("Transaction confirmed"))
            }
        }
    }

    private fun saveRecurring() {
        val form = _uiState.value.formState
        val amount = form.amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L

        viewModelScope.launch {
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = true)) }
            val recurring = RecurringTransaction(
                id = form.id,
                name = form.name,
                amount = amount,
                type = form.type,
                recurrenceType = form.frequency,
                sourceWalletId = form.sourceWalletId,
                destinationWalletId = form.destinationWalletId,
                categoryId = form.categoryId,
                startDate = form.startDate,
                lastExecutedDate = null,
                nextExecutionDate = form.startDate,
                isActive = form.isActive,
                note = form.note
            )

            val result = if (form.isEditMode) {
                updateRecurringTransactionUseCase(recurring)
                Resource.Success(recurring.id)
            } else {
                addRecurringTransactionUseCase(recurring)
            }

            when (result) {
                is Resource.Success -> {
                    _uiEffect.send(RecurringUiEffect.ShowSnackbar("Rule saved"))
                    _uiEffect.send(RecurringUiEffect.NavigateBack)
                }
                is Resource.Error -> {
                    _uiEffect.send(RecurringUiEffect.ShowSnackbar("Error: ${result.error}"))
                }
                else -> {}
            }
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = false)) }
        }
    }
}
