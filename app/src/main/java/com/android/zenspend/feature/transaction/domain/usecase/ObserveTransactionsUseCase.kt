package com.android.zenspend.feature.transaction.domain.usecase

import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
}
