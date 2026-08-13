package com.android.zenspend.feature.recurring.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import java.util.Calendar
import javax.inject.Inject

class ConfirmRecurringOccurrenceUseCase @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: com.android.zenspend.feature.transaction.domain.repository.TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateMerchantPreferenceUseCase: com.android.zenspend.feature.category.domain.usecase.UpdateMerchantPreferenceUseCase
) {
    suspend operator fun invoke(recurringId: Long, occurrenceDate: Long): Resource<Unit> {
        val recurring = recurringRepository.getRecurringTransactionById(recurringId)
            ?: return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("Recurring rule not found"))

        if (!recurring.isActive) {
            return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("Recurring rule is paused"))
        }

        val occurrenceKey = "${recurring.id}_$occurrenceDate"
        
        // Idempotency check: check if already exists in transaction repository
        val existing = transactionRepository.getTransactionByOccurrenceKey(occurrenceKey)
        if (existing != null) {
            return Resource.Error(com.android.zenspend.core.common.error.AppError.ValidationError.GenericError("This occurrence has already been confirmed"))
        }

        // 1. Create Transaction
        val transaction = Transaction(
            title = recurring.name,
            amount = recurring.amount,
            date = occurrenceDate,
            note = recurring.note,
            type = recurring.type,
            sourceWalletId = recurring.sourceWalletId,
            destinationWalletId = recurring.destinationWalletId,
            categoryId = recurring.categoryId,
            source = TransactionSource.RECURRING,
            recurringTransactionId = recurring.id,
            recurringOccurrenceKey = occurrenceKey
        )

        // 2. Add to Ledger (AddTransactionUseCase handles validation and balance updates)
        val result = addTransactionUseCase(transaction)
        
        return when (result) {
            is Resource.Success -> {
                // 3. Update learning model
                updateMerchantPreferenceUseCase(
                    merchant = recurring.name,
                    title = recurring.name,
                    categoryId = recurring.categoryId
                )

                // 4. Advance Rule
                val nextDate = calculateNextOccurrence(occurrenceDate, recurring.recurrenceType)
                val updatedRecurring = recurring.copy(
                    lastExecutedDate = occurrenceDate,
                    nextExecutionDate = nextDate,
                    updatedAt = System.currentTimeMillis()
                )
                recurringRepository.updateRecurringTransaction(updatedRecurring)
                Resource.Success(Unit)
            }
            is Resource.Error -> {
                // If it failed due to unique constraint, we consider it "already confirmed" or error
                Resource.Error(result.error)
            }
            else -> Resource.Loading
        }
    }

    private fun calculateNextOccurrence(current: Long, frequency: RecurrenceType): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = current
        when (frequency) {
            RecurrenceType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurrenceType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurrenceType.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            RecurrenceType.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
