package com.android.zenspend.feature.people.data

import com.android.zenspend.core.database.dao.PeopleDao
import com.android.zenspend.core.database.entity.LoanReceiptEntity
import com.android.zenspend.core.database.entity.PersonEntity
import com.android.zenspend.core.database.entity.PersonLoanEntity
import com.android.zenspend.feature.people.domain.model.LoanReceipt
import com.android.zenspend.feature.people.domain.model.LoanSchedule
import com.android.zenspend.feature.people.domain.model.Person
import com.android.zenspend.feature.people.domain.model.PersonLoan
import com.android.zenspend.feature.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PeopleRepositoryImpl @Inject constructor(
    private val dao: PeopleDao
) : PeopleRepository {

    override fun observePeople(): Flow<List<Person>> = dao.observePeople().map { l -> l.map { it.toDomain() } }
    override fun observePerson(id: Long): Flow<Person?> = dao.observePerson(id).map { it?.toDomain() }
    override suspend fun addPerson(person: Person): Long = dao.insertPerson(person.toEntity())
    override suspend fun deletePerson(id: Long) = dao.deletePerson(id)

    override fun observeLoans(): Flow<List<PersonLoan>> = dao.observeLoans().map { l -> l.map { it.toDomain() } }
    override fun observeLoansOf(personId: Long): Flow<List<PersonLoan>> =
        dao.observeLoansOf(personId).map { l -> l.map { it.toDomain() } }
    override fun observeLoan(id: Long): Flow<PersonLoan?> = dao.observeLoan(id).map { it?.toDomain() }
    override suspend fun getLoan(id: Long): PersonLoan? = dao.getLoan(id)?.toDomain()

    override suspend fun saveLoan(loan: PersonLoan): Long =
        if (loan.id == 0L) dao.insertLoan(loan.toEntity()) else {
            dao.updateLoan(loan.toEntity())
            loan.id
        }

    override suspend fun closeLoan(id: Long) = dao.closeLoan(id)
    override suspend fun deleteLoan(id: Long) = dao.deleteLoan(id)

    override fun observeReceipts(loanId: Long): Flow<List<LoanReceipt>> =
        dao.observeReceipts(loanId).map { l -> l.map { it.toDomain() } }

    override suspend fun receiveNextInstallment(loanId: Long, receivedDate: Long): Boolean {
        val loan = dao.getLoan(loanId)?.toDomain() ?: return false
        if (!LoanSchedule.isActive(loan)) return false
        val number = loan.receivedInstallments + 1
        dao.addReceipt(
            LoanReceiptEntity(
                loanId = loanId,
                installmentNumber = number,
                amount = loan.installmentAmount,
                dueDate = LoanSchedule.dueDate(loan, number),
                receivedDate = receivedDate
            ),
            tenure = loan.totalTenure
        )
        return true
    }

    override suspend fun undoLastReceipt(loanId: Long): Boolean = dao.undoLastReceipt(loanId)
}

private fun PersonEntity.toDomain() = Person(id, name, phone, note)
private fun Person.toEntity() = PersonEntity(id = id, name = name.trim(), phone = phone?.trim()?.ifBlank { null }, note = note)

private fun PersonLoanEntity.toDomain() = PersonLoan(
    id, personId, title, principalAmount, installmentAmount, totalTenure,
    receivedInstallments, startDate, dueDay, isClosed, note
)

private fun PersonLoan.toEntity() = PersonLoanEntity(
    id = id, personId = personId, title = title, principalAmount = principalAmount,
    installmentAmount = installmentAmount, totalTenure = totalTenure,
    receivedInstallments = receivedInstallments, startDate = startDate, dueDay = dueDay,
    isClosed = isClosed, note = note
)

private fun LoanReceiptEntity.toDomain() = LoanReceipt(id, loanId, installmentNumber, amount, dueDate, receivedDate)
