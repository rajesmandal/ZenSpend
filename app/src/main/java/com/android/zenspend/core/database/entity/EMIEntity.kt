package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.EMIStatus

@Entity(
    tableName = "emis",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["walletId"])]
)
data class EMIEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val walletId: Long?, // Linked account/card
    val principalAmount: Long,
    val emiAmount: Long,
    val interestRate: Double?,
    val totalTenure: Int,
    val remainingInstallments: Int,
    val startDate: Long,
    val endDate: Long,
    val dueDay: Int,
    val status: EMIStatus = EMIStatus.ACTIVE
)
