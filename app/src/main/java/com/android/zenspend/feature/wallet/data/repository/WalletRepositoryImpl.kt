package com.android.zenspend.feature.wallet.data.repository

import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.dao.WalletDao
import com.android.zenspend.feature.wallet.data.mapper.toDomain
import com.android.zenspend.feature.wallet.data.mapper.toEntity
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao
) : WalletRepository {

    override fun getAllActiveWallets(): Flow<List<Wallet>> {
        return walletDao.getAllActiveWallets().map { list ->
            list.map { it.wallet.toDomain().copy(currentBalance = it.currentBalance) }
        }
    }

    override suspend fun getWalletById(id: Long): Wallet? {
        return walletDao.getWalletById(id)?.let { it.wallet.toDomain().copy(currentBalance = it.currentBalance) }
    }

    override suspend fun insertWallet(wallet: Wallet): Long {
        return walletDao.insertWallet(wallet.toEntity())
    }

    override suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }

    override suspend fun archiveWallet(id: Long) {
        walletDao.softDeleteWallet(id)
    }

    override fun observeWalletBalance(walletId: Long): Flow<Long?> {
        return transactionDao.getCalculatedBalance(walletId)
    }
}
