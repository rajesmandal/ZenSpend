package com.android.zenspend.feature.wallet.domain.repository

import com.android.zenspend.feature.wallet.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getAllActiveWallets(): Flow<List<Wallet>>
    suspend fun getWalletById(id: Long): Wallet?
    suspend fun insertWallet(wallet: Wallet): Long
    suspend fun updateWallet(wallet: Wallet)
    suspend fun archiveWallet(id: Long)
    fun observeWalletBalance(walletId: Long): Flow<Long?>
}
