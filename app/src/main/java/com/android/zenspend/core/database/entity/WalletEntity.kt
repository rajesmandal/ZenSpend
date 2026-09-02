package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.SyncState
import com.android.zenspend.core.database.model.WalletType

@Entity(
    tableName = "wallets",
    indices = [
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val openingBalance: Long, // in minor units
    val currency: String = "INR",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
