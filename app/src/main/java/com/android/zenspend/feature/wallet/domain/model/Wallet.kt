package com.android.zenspend.feature.wallet.domain.model

import com.android.zenspend.core.database.model.WalletType

data class Wallet(
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val openingBalance: Long,
    val currentBalance: Long,
    val currency: String = "INR",
    val isActive: Boolean = true
)
