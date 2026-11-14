package com.android.zenspend.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.android.zenspend.core.database.entity.LoanReceiptEntity
import com.android.zenspend.core.database.entity.PersonEntity
import com.android.zenspend.core.database.entity.PersonLoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PeopleDao {
    @Query("SELECT * FROM people ORDER BY name COLLATE NOCASE ASC")
    abstract fun observePeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id")
    abstract fun observePerson(id: Long): Flow<PersonEntity?>

    @Insert
    abstract suspend fun insertPerson(person: PersonEntity): Long

    @Query("DELETE FROM people WHERE id = :id")
    abstract suspend fun deletePerson(id: Long)

    @Query("SELECT * FROM person_loans ORDER BY isClosed ASC, id DESC")
    abstract fun observeLoans(): Flow<List<PersonLoanEntity>>

    @Query("SELECT * FROM person_loans WHERE personId = :personId ORDER BY isClosed ASC, id DESC")
    abstract fun observeLoansOf(personId: Long): Flow<List<PersonLoanEntity>>

    @Query("SELECT * FROM person_loans WHERE id = :id")
    abstract fun observeLoan(id: Long): Flow<PersonLoanEntity?>

    @Query("SELECT * FROM person_loans WHERE id = :id")
    abstract suspend fun getLoan(id: Long): PersonLoanEntity?

    @Insert
    abstract suspend fun insertLoan(loan: PersonLoanEntity): Long

    @Update
    abstract suspend fun updateLoan(loan: PersonLoanEntity)

    @Query("DELETE FROM person_loans WHERE id = :id")
    abstract suspend fun deleteLoan(id: Long)

    @Query("UPDATE person_loans SET isClosed = 1 WHERE id = :id")
    abstract suspend fun closeLoan(id: Long)

    @Query("SELECT * FROM person_loan_receipts WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    abstract fun observeReceipts(loanId: Long): Flow<List<LoanReceiptEntity>>

    @Query("SELECT * FROM person_loan_receipts WHERE loanId = :loanId ORDER BY installmentNumber DESC LIMIT 1")
    abstract suspend fun getLastReceipt(loanId: Long): LoanReceiptEntity?

    @Insert
    abstract suspend fun insertReceipt(receipt: LoanReceiptEntity): Long

    @Query("DELETE FROM person_loan_receipts WHERE id = :id")
    abstract suspend fun deleteReceipt(id: Long)

    @Query("UPDATE person_loans SET receivedInstallments = :received, isClosed = :closed WHERE id = :id")
    abstract suspend fun setProgress(id: Long, received: Int, closed: Boolean)

    /** Stores the receipt and moves the loan forward (closing it with the last installment) as one step. */
    @Transaction
    open suspend fun addReceipt(receipt: LoanReceiptEntity, tenure: Int) {
        insertReceipt(receipt)
        setProgress(receipt.loanId, receipt.installmentNumber, receipt.installmentNumber >= tenure)
    }

    /** Takes back the most recent receipt and re-opens the loan. Returns false when there was none. */
    @Transaction
    open suspend fun undoLastReceipt(loanId: Long): Boolean {
        val last = getLastReceipt(loanId) ?: return false
        deleteReceipt(last.id)
        setProgress(loanId, last.installmentNumber - 1, false)
        return true
    }
}
