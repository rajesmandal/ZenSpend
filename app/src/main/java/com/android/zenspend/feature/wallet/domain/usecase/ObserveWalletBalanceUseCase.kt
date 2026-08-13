package com.android.zenspend.feature.wallet.domain.usecase

import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWalletBalanceUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    operator fun invoke(walletId: Long): Flow<Long?> = repository.observeWalletBalance(walletId)
}
