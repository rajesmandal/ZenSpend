package com.android.zenspend.core.database.model

data class WalletActivityEntity(
    val walletId: Long,
    val walletName: String,
    val incomeAmount: Long,
    val expenseAmount: Long,
    val netMovement: Long
)
