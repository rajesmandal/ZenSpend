package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One installment that has been paid. Deleted together with its EMI. */
@Entity(
    tableName = "emi_payments",
    foreignKeys = [
        ForeignKey(
            entity = EMIEntity::class,
            parentColumns = ["id"],
            childColumns = ["emiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["emiId"])]
)
data class EMIPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val emiId: Long,
    val installmentNumber: Int,
    val amount: Long,
    val dueDate: Long,
    val paidDate: Long,
    /** Wallet the money was taken from, if the user chose to record it. */
    val walletId: Long? = null,
    /** Expense transaction created for this payment, if any. */
    val transactionId: Long? = null
)
