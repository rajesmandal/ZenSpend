package com.android.zenspend.feature.transaction.domain.usecase

import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.model.withNormalizedWallets
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) = repository.updateTransaction(transaction.withNormalizedWallets())
}
