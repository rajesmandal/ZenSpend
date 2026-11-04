package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.core.database.model.SyncState

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
    indices = [
        Index(value = ["walletId"]),
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class EMIEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val walletId: Long?,
    val principalAmount: Long,
    val emiAmount: Long,
    val interestRate: Double?,
    val totalTenure: Int,
    val remainingInstallments: Int,
    val startDate: Long,
    val endDate: Long,
    val dueDay: Int,
    val status: EMIStatus = EMIStatus.ACTIVE,
    val lenderType: EMILenderType = EMILenderType.CREDIT_CARD,
    /** Bank / card / person the EMI is owed to, e.g. "HDFC Bank" or "Ramesh". */
    val lenderName: String = "",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
