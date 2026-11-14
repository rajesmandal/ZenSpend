package com.android.zenspend.feature.people.domain.repository

import com.android.zenspend.feature.people.domain.model.LoanReceipt
import com.android.zenspend.feature.people.domain.model.Person
import com.android.zenspend.feature.people.domain.model.PersonLoan
import kotlinx.coroutines.flow.Flow

interface PeopleRepository {
    fun observePeople(): Flow<List<Person>>
    fun observePerson(id: Long): Flow<Person?>
    suspend fun addPerson(person: Person): Long
    suspend fun deletePerson(id: Long)

    fun observeLoans(): Flow<List<PersonLoan>>
    fun observeLoansOf(personId: Long): Flow<List<PersonLoan>>
    fun observeLoan(id: Long): Flow<PersonLoan?>
    suspend fun getLoan(id: Long): PersonLoan?
    suspend fun saveLoan(loan: PersonLoan): Long
    suspend fun closeLoan(id: Long)
    suspend fun deleteLoan(id: Long)

    fun observeReceipts(loanId: Long): Flow<List<LoanReceipt>>

    /** Marks the next installment as received. Returns false when the loan is not active. */
    suspend fun receiveNextInstallment(loanId: Long, receivedDate: Long = System.currentTimeMillis()): Boolean
    suspend fun undoLastReceipt(loanId: Long): Boolean
}
