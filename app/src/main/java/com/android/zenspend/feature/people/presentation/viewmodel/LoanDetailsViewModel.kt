package com.android.zenspend.feature.people.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.feature.people.domain.repository.PeopleRepository
import com.android.zenspend.feature.people.presentation.contract.LoanDetailsUiEffect
import com.android.zenspend.feature.people.presentation.contract.LoanDetailsUiEvent
import com.android.zenspend.feature.people.presentation.contract.LoanDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PeopleRepository
) : ViewModel() {

    private val loanId: Long = savedStateHandle.get<String>("loanId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(LoanDetailsUiState())
    val uiState: StateFlow<LoanDetailsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<LoanDetailsUiEffect>()
    val uiEffect: Flow<LoanDetailsUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeLoan(loanId), repository.observeReceipts(loanId)) { loan, receipts ->
                val name = loan?.let { repository.observePerson(it.personId).first()?.name }.orEmpty()
                LoanDetailsUiState(isLoading = false, personName = name, loan = loan, receipts = receipts)
            }.collect { _uiState.value = it }
        }
    }

    fun handleEvent(event: LoanDetailsUiEvent) {
        viewModelScope.launch {
            when (event) {
                LoanDetailsUiEvent.MarkReceived ->
                    if (repository.receiveNextInstallment(loanId)) message(R.string.loan_message_received)
                LoanDetailsUiEvent.UndoLast ->
                    message(if (repository.undoLastReceipt(loanId)) R.string.loan_message_undone else R.string.loan_message_nothing_to_undo)
                LoanDetailsUiEvent.Close -> repository.closeLoan(loanId)
                LoanDetailsUiEvent.Delete -> {
                    repository.deleteLoan(loanId)
                    _uiEffect.send(LoanDetailsUiEffect.NavigateBack)
                }
            }
        }
    }

    private suspend fun message(res: Int) = _uiEffect.send(LoanDetailsUiEffect.ShowMessage(res))
}
