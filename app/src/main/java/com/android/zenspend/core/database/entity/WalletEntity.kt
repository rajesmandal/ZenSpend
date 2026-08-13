package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val openingBalance: Long, // in minor units
    val currentBalance: Long, // in minor units
    val currency: String = "INR",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
