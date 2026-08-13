package com.android.zenspend.feature.wallet.domain.usecase

import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWalletsUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    operator fun invoke(): Flow<List<Wallet>> = repository.getAllActiveWallets()
}
