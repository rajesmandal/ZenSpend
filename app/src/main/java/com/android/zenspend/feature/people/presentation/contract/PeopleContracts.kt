package com.android.zenspend.feature.people.presentation.contract

import com.android.zenspend.feature.people.domain.model.LoanReceipt
import com.android.zenspend.feature.people.domain.model.PersonLoan
import com.android.zenspend.feature.people.domain.model.PersonOverview
import com.android.zenspend.feature.people.domain.model.ReceivablesSummary

// ---- People list ----
data class PeopleUiState(
    val isLoading: Boolean = true,
    val people: List<PersonOverview> = emptyList()
) {
    val summary: ReceivablesSummary get() = ReceivablesSummary.of(people)
}

sealed interface PeopleUiEvent {
    data class AddPerson(val name: String, val phone: String) : PeopleUiEvent
}

// ---- Person details ----
data class PersonDetailsUiState(
    val isLoading: Boolean = true,
    val overview: PersonOverview? = null
)

sealed interface PersonDetailsUiEvent {
    object Delete : PersonDetailsUiEvent
}

sealed interface PersonDetailsUiEffect {
    object NavigateBack : PersonDetailsUiEffect
}

// ---- Loan form ----
data class LoanFormUiState(
    val loanId: Long? = null,
    val personName: String = "",
    val title: String = "",
    val principalText: String = "",
    val installmentText: String = "",
    val tenureText: String = "",
    val dueDayText: String = "1",
    val receivedText: String = "0",
    val startDateMillis: Long = System.currentTimeMillis(),
    val startDateLabel: String = "",
    val noteText: String = "",
    val isSubmitting: Boolean = false,
    val errorRes: Int? = null
) {
    val isEdit: Boolean get() = loanId != null
}

sealed interface LoanFormUiEvent {
    data class Init(val personId: Long, val loanId: Long) : LoanFormUiEvent
    data class Title(val v: String) : LoanFormUiEvent
    data class Principal(val v: String) : LoanFormUiEvent
    data class Installment(val v: String) : LoanFormUiEvent
    data class Tenure(val v: String) : LoanFormUiEvent
    data class DueDay(val v: String) : LoanFormUiEvent
    data class Received(val v: String) : LoanFormUiEvent
    data class StartDate(val millis: Long) : LoanFormUiEvent
    data class Note(val v: String) : LoanFormUiEvent
    object Save : LoanFormUiEvent
}

sealed interface LoanFormUiEffect {
    object NavigateBack : LoanFormUiEffect
}

// ---- Loan details ----
data class LoanDetailsUiState(
    val isLoading: Boolean = true,
    val personName: String = "",
    val loan: PersonLoan? = null,
    val receipts: List<LoanReceipt> = emptyList()
)

sealed interface LoanDetailsUiEvent {
    object MarkReceived : LoanDetailsUiEvent
    object UndoLast : LoanDetailsUiEvent
    object Close : LoanDetailsUiEvent
    object Delete : LoanDetailsUiEvent
}

sealed interface LoanDetailsUiEffect {
    data class ShowMessage(val messageRes: Int) : LoanDetailsUiEffect
    object NavigateBack : LoanDetailsUiEffect
}
