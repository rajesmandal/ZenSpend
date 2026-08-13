package com.android.zenspend.feature.transaction.domain.usecase

import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Resource<Long> {
        // 1. Common Validation
        if (transaction.amount <= 0) {
            return Resource.Error(AppError.ValidationError.InvalidAmount)
        }

        // 2. Type-specific Validation
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                if (transaction.sourceWalletId == null || transaction.sourceWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingSourceWallet)
                }
            }
            TransactionType.INCOME -> {
                if (transaction.destinationWalletId == null || transaction.destinationWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
                }
            }
            TransactionType.TRANSFER -> {
                if (transaction.sourceWalletId == null || transaction.sourceWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingSourceWallet)
                }
                if (transaction.destinationWalletId == null || transaction.destinationWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
                }
                if (transaction.sourceWalletId == transaction.destinationWalletId) {
                    return Resource.Error(AppError.ValidationError.SameWalletTransfer)
                }
            }
            TransactionType.CREDIT_CARD_PAYMENT -> {
                if (transaction.sourceWalletId == null || transaction.sourceWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingSourceWallet)
                }
                if (transaction.destinationWalletId == null || transaction.destinationWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
                }
            }
            TransactionType.REFUND -> {
                if (transaction.destinationWalletId == null || transaction.destinationWalletId == 0L) {
                    return Resource.Error(AppError.ValidationError.MissingDestinationWallet)
                }
            }
        }

        return try {
            val id = repository.insertTransaction(transaction)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(AppError.DatabaseError.GenericError(e.message ?: "Unknown database error"))
        }
    }
}
