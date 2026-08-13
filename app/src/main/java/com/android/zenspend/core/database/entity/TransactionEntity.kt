package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceWalletId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationWalletId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = EMIEntity::class,
            parentColumns = ["id"],
            childColumns = ["emiId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RecurringTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurringTransactionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["sourceWalletId"]),
        Index(value = ["destinationWalletId"]),
        Index(value = ["categoryId"]),
        Index(value = ["emiId"]),
        Index(value = ["recurringTransactionId"]),
        Index(value = ["recurringOccurrenceKey"], unique = true),
        Index(value = ["date"]),
        Index(value = ["type"]),
        Index(value = ["externalReference"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long, // in minor units
    val date: Long, // Timestamp
    val note: String?,
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val source: TransactionSource = TransactionSource.MANUAL,
    
    val sourceWalletId: Long? = null, // Debit from
    val destinationWalletId: Long? = null, // Credit to (for transfers/CC payments)
    val categoryId: Long? = null,
    val emiId: Long? = null,
    val recurringTransactionId: Long? = null,
    val recurringOccurrenceKey: String? = null,
    val originalTransactionId: Long? = null, // For linking refunds to original expenses
    
    val externalReference: String? = null, // For SMS/External IDs
    val originalMessage: String? = null, // Raw SMS text if any
    val isAutoDetected: Boolean = false,
    val isUserConfirmed: Boolean = true,
    val isExcludedFromReports: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
