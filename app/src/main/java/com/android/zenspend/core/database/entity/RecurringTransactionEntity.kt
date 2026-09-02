package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.SyncState
import com.android.zenspend.core.database.model.TransactionType

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceWalletId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationWalletId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["sourceWalletId"]),
        Index(value = ["destinationWalletId"]),
        Index(value = ["categoryId"]),
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Long,
    val type: TransactionType,
    val recurrenceType: RecurrenceType,
    val sourceWalletId: Long?,
    val destinationWalletId: Long?,
    val categoryId: Long?,
    val startDate: Long,
    val lastExecutedDate: Long?,
    val nextExecutionDate: Long,
    val isActive: Boolean = true,
    val note: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
