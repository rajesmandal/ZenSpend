package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [Index(value = ["walletId"])]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletId: Long,
    val bankName: String,
    val lastFourDigits: String?,
    val creditLimit: Long, // in minor units
    val billingCycleDate: Int, // 1-31
    val paymentDueDate: Int, // 1-31
    val active: Boolean = true
)
