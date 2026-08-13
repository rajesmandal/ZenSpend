package com.android.zenspend.feature.recurring.domain.usecase

import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class AddRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    suspend operator fun invoke(recurring: RecurringTransaction): Resource<Long> {
        if (recurring.amount <= 0) return Resource.Error(AppError.ValidationError.InvalidAmount)
        if (recurring.name.isBlank()) return Resource.Error(AppError.ValidationError.GenericError("Name required"))
        
        when (recurring.type) {
            TransactionType.EXPENSE -> if (recurring.sourceWalletId == null) return Resource.Error(AppError.ValidationError.MissingSourceWallet)
            TransactionType.INCOME -> if (recurring.destinationWalletId == null) return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
            TransactionType.TRANSFER -> {
                if (recurring.sourceWalletId == null) return Resource.Error(AppError.ValidationError.MissingSourceWallet)
                if (recurring.destinationWalletId == null) return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
                if (recurring.sourceWalletId == recurring.destinationWalletId) return Resource.Error(AppError.ValidationError.SameWalletTransfer)
            }
            else -> {}
        }

        return try {
            val id = repository.insertRecurringTransaction(recurring)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(AppError.DatabaseError.GenericError(e.message ?: "Unknown error"))
        }
    }
}

class UpdateRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    suspend operator fun invoke(recurring: RecurringTransaction) {
        repository.updateRecurringTransaction(recurring)
    }
}

class ArchiveRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.archiveRecurringTransaction(id)
    }
}

class SetRecurringStatusUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: Long, isActive: Boolean) {
        repository.setRecurringTransactionStatus(id, isActive)
    }
}
