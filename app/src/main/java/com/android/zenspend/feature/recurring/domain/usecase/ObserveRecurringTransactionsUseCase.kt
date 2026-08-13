package com.android.zenspend.feature.recurring.domain.usecase

import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecurringTransactionsUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    operator fun invoke(includeInactive: Boolean = false): Flow<List<RecurringTransaction>> {
        return if (includeInactive) {
            repository.getAllRecurringTransactions()
        } else {
            repository.getAllActiveRecurringTransactions()
        }
    }
}
