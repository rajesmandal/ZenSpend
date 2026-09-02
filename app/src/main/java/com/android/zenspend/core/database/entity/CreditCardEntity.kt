package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.SyncState

@Entity(
    tableName = "credit_cards",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["walletId"]),
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletId: Long,
    val bankName: String,
    val lastFourDigits: String?,
    val creditLimit: Long,
    val billingCycleDate: Int,
    val paymentDueDate: Int,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
