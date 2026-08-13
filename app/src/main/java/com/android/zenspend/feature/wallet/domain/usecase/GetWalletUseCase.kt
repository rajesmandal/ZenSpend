package com.android.zenspend.feature.wallet.domain.usecase

import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(id: Long): Wallet? = repository.getWalletById(id)
}
