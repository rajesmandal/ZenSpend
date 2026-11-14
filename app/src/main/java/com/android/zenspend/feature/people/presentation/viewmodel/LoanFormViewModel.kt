package com.android.zenspend.feature.people.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.core.ui.util.MoneyFormat
import com.android.zenspend.feature.people.domain.model.PersonLoan
import com.android.zenspend.feature.people.domain.repository.PeopleRepository
import com.android.zenspend.feature.people.presentation.contract.LoanFormUiEffect
import com.android.zenspend.feature.people.presentation.contract.LoanFormUiEvent
import com.android.zenspend.feature.people.presentation.contract.LoanFormUiState
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
class LoanFormViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanFormUiState())
    val uiState: StateFlow<LoanFormUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<LoanFormUiEffect>()
    val uiEffect: Flow<LoanFormUiEffect> = _uiEffect.receiveAsFlow()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var initialised = false
    private var personId = 0L
    private var existing: PersonLoan? = null

    fun handleEvent(event: LoanFormUiEvent) {
        when (event) {
            is LoanFormUiEvent.Init -> init(event.personId, event.loanId)
            is LoanFormUiEvent.Title -> _uiState.update { it.copy(title = event.v) }
            is LoanFormUiEvent.Principal -> _uiState.update { it.copy(principalText = event.v, errorRes = null) }
            is LoanFormUiEvent.Installment -> _uiState.update { it.copy(installmentText = event.v, errorRes = null) }
            is LoanFormUiEvent.Tenure -> _uiState.update { it.copy(tenureText = event.v.filter(Char::isDigit), errorRes = null) }
            is LoanFormUiEvent.DueDay -> _uiState.update { it.copy(dueDayText = event.v.filter(Char::isDigit), errorRes = null) }
            is LoanFormUiEvent.Received -> _uiState.update { it.copy(receivedText = event.v.filter(Char::isDigit), errorRes = null) }
            is LoanFormUiEvent.StartDate -> _uiState.update {
                it.copy(startDateMillis = event.millis, startDateLabel = dateFormat.format(Date(event.millis)))
            }
            is LoanFormUiEvent.Note -> _uiState.update { it.copy(noteText = event.v) }
            LoanFormUiEvent.Save -> save()
        }
    }

    private fun init(personId: Long, loanId: Long) {
        if (initialised) return
        initialised = true
        viewModelScope.launch {
            val loan = if (loanId > 0) repository.getLoan(loanId) else null
            existing = loan
            this@LoanFormViewModel.personId = loan?.personId ?: personId
            val person = repository.observePerson(this@LoanFormViewModel.personId).first()
            val now = System.currentTimeMillis()
            _uiState.update {
                if (loan == null) {
                    it.copy(personName = person?.name.orEmpty(), startDateMillis = now, startDateLabel = dateFormat.format(Date(now)))
                } else {
                    it.copy(
                        loanId = loan.id,
                        personName = person?.name.orEmpty(),
                        title = loan.title,
                        principalText = MoneyFormat.rawFromMinor(loan.principalAmount),
                        installmentText = MoneyFormat.rawFromMinor(loan.installmentAmount),
                        tenureText = loan.totalTenure.toString(),
                        dueDayText = loan.dueDay.toString(),
                        receivedText = loan.receivedInstallments.toString(),
                        startDateMillis = loan.startDate,
                        startDateLabel = dateFormat.format(Date(loan.startDate)),
                        noteText = loan.note.orEmpty()
                    )
                }
            }
        }
    }

    private fun save() {
        val s = _uiState.value
        val principal = parseMinor(s.principalText) ?: return fail(R.string.loan_error_amount)
        val installment = parseMinor(s.installmentText) ?: return fail(R.string.loan_error_installment)
        val tenure = s.tenureText.toIntOrNull()?.takeIf { it > 0 } ?: return fail(R.string.loan_error_tenure)
        val dueDay = s.dueDayText.toIntOrNull()?.takeIf { it in 1..MAX_DAY } ?: return fail(R.string.loan_error_due_day)
        val received = s.receivedText.toIntOrNull() ?: 0
        if (received > tenure) return fail(R.string.loan_error_received)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorRes = null) }
            repository.saveLoan(
                PersonLoan(
                    id = existing?.id ?: 0L,
                    personId = personId,
                    title = s.title.trim(),
                    principalAmount = principal,
                    installmentAmount = installment,
                    totalTenure = tenure,
                    receivedInstallments = received,
                    startDate = s.startDateMillis,
                    dueDay = dueDay,
                    isClosed = existing?.isClosed == true && received < tenure,
                    note = s.noteText.trim().ifBlank { null }
                )
            )
            _uiEffect.send(LoanFormUiEffect.NavigateBack)
        }
    }

    private fun fail(res: Int) = _uiState.update { it.copy(errorRes = res) }

    private fun parseMinor(text: String): Long? {
        val value = text.toDoubleOrNull() ?: return null
        return if (value > 0) Math.round(value * MINOR_PER_MAJOR) else null
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100
        const val MAX_DAY = 31
    }
}
