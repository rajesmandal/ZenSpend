package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Money given to a person that they repay in monthly installments. Deleted with the person. */
@Entity(
    tableName = "person_loans",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class PersonLoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val title: String,
    val principalAmount: Long,
    val installmentAmount: Long,
    val totalTenure: Int,
    /** How many installments the person has already paid back. */
    val receivedInstallments: Int,
    val startDate: Long,
    val dueDay: Int,
    /** Closed by hand (e.g. settled early / written off); completed loans close themselves. */
    val isClosed: Boolean,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
