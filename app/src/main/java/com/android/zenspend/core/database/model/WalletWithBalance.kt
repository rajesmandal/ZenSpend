package com.android.zenspend.core.database.model

import com.android.zenspend.core.database.entity.WalletEntity

data class WalletWithBalance(
    @androidx.room.Embedded
    val wallet: WalletEntity,
    val currentBalance: Long
)
