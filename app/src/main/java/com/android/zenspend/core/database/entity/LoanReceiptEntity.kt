package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One installment the person has paid back. Deleted together with its loan. */
@Entity(
    tableName = "person_loan_receipts",
    foreignKeys = [
        ForeignKey(
            entity = PersonLoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class LoanReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int,
    val amount: Long,
    val dueDate: Long,
    val receivedDate: Long
)
