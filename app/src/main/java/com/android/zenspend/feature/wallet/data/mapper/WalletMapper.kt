package com.android.zenspend.feature.wallet.data.mapper

import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.feature.wallet.domain.model.Wallet

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        id = id,
        name = name,
        type = type,
        openingBalance = openingBalance,
        currentBalance = 0, // Should be populated by repository if needed
        currency = currency,
        isActive = isActive
    )
}

fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        id = id,
        name = name,
        type = type,
        openingBalance = openingBalance,
        currency = currency,
        isActive = isActive
    )
}
