package com.android.zenspend.feature.wallet.presentation.model

import com.android.zenspend.core.database.model.WalletType

data class WalletUiModel(
    val id: Long,
    val name: String,
    val type: WalletType,
    val balance: String, // Formatted balance
    val rawBalance: Long,
    val currency: String,
    val isActive: Boolean,
    val icon: String? = null,
    val color: Int? = null
) {
    val isLiability: Boolean get() = type == WalletType.CREDIT_CARD
}
