package com.android.zenspend.feature.backup.data.mapper

import com.android.zenspend.core.database.entity.*
import com.android.zenspend.feature.backup.domain.model.*

fun WalletEntity.toBackup() = BackupWallet(
    id = id, name = name, type = type, openingBalance = openingBalance,
    currentBalance = currentBalance, currency = currency, isActive = isActive,
    createdAt = createdAt, updatedAt = updatedAt
)

fun BackupWallet.toEntity() = WalletEntity(
    id = id, name = name, type = type, openingBalance = openingBalance,
    currentBalance = currentBalance, currency = currency, isActive = isActive,
    createdAt = createdAt, updatedAt = updatedAt
)

fun CategoryEntity.toBackup() = BackupCategory(
    id = id, name = name, icon = icon, color = color, type = type,
    isActive = isActive, isDefault = isDefault
)

fun BackupCategory.toEntity() = CategoryEntity(
    id = id, name = name, icon = icon, color = color, type = type,
    isActive = isActive, isDefault = isDefault
)

fun CreditCardEntity.toBackup() = BackupCreditCard(
    id = id, walletId = walletId, bankName = bankName, lastFourDigits = lastFourDigits,
    creditLimit = creditLimit, billingCycleDate = billingCycleDate,
    paymentDueDate = paymentDueDate, active = active
)

fun BackupCreditCard.toEntity() = CreditCardEntity(
    id = id, walletId = walletId, bankName = bankName, lastFourDigits = lastFourDigits,
    creditLimit = creditLimit, billingCycleDate = billingCycleDate,
    paymentDueDate = paymentDueDate, active = active
)

fun EMIEntity.toBackup() = BackupEMI(
    id = id, name = name, walletId = walletId, principalAmount = principalAmount,
    emiAmount = emiAmount, interestRate = interestRate, totalTenure = totalTenure,
    remainingInstallments = remainingInstallments, startDate = startDate,
    endDate = endDate, dueDay = dueDay, status = status
)

fun BackupEMI.toEntity() = EMIEntity(
    id = id, name = name, walletId = walletId, principalAmount = principalAmount,
    emiAmount = emiAmount, interestRate = interestRate, totalTenure = totalTenure,
    remainingInstallments = remainingInstallments, startDate = startDate,
    endDate = endDate, dueDay = dueDay, status = status
)

fun RecurringTransactionEntity.toBackup() = BackupRecurringTransaction(
    id = id, name = name, amount = amount, type = type, recurrenceType = recurrenceType,
    sourceWalletId = sourceWalletId, destinationWalletId = destinationWalletId,
    categoryId = categoryId, startDate = startDate, lastExecutedDate = lastExecutedDate,
    nextExecutionDate = nextExecutionDate, isActive = isActive, note = note,
    createdAt = createdAt, updatedAt = updatedAt
)

fun BackupRecurringTransaction.toEntity() = RecurringTransactionEntity(
    id = id, name = name, amount = amount, type = type, recurrenceType = recurrenceType,
    sourceWalletId = sourceWalletId, destinationWalletId = destinationWalletId,
    categoryId = categoryId, startDate = startDate, lastExecutedDate = lastExecutedDate,
    nextExecutionDate = nextExecutionDate, isActive = isActive, note = note,
    createdAt = createdAt, updatedAt = updatedAt
)

fun TagEntity.toBackup() = BackupTag(
    id = id, name = name, color = color
)

fun BackupTag.toEntity() = TagEntity(
    id = id, name = name, color = color
)

fun TransactionEntity.toBackup() = BackupTransaction(
    id = id, title = title, amount = amount, date = date, note = note, type = type,
    status = status, source = source, sourceWalletId = sourceWalletId,
    destinationWalletId = destinationWalletId, categoryId = categoryId, emiId = emiId,
    recurringTransactionId = recurringTransactionId, recurringOccurrenceKey = recurringOccurrenceKey,
    originalTransactionId = originalTransactionId, externalReference = externalReference,
    originalMessage = originalMessage, isAutoDetected = isAutoDetected,
    isUserConfirmed = isUserConfirmed, isExcludedFromReports = isExcludedFromReports,
    createdAt = createdAt, updatedAt = updatedAt
)

fun BackupTransaction.toEntity() = TransactionEntity(
    id = id, title = title, amount = amount, date = date, note = note, type = type,
    status = status, source = source, sourceWalletId = sourceWalletId,
    destinationWalletId = destinationWalletId, categoryId = categoryId, emiId = emiId,
    recurringTransactionId = recurringTransactionId, recurringOccurrenceKey = recurringOccurrenceKey,
    originalTransactionId = originalTransactionId, externalReference = externalReference,
    originalMessage = originalMessage, isAutoDetected = isAutoDetected,
    isUserConfirmed = isUserConfirmed, isExcludedFromReports = isExcludedFromReports,
    createdAt = createdAt, updatedAt = updatedAt
)

fun TransactionTagCrossRef.toBackup() = BackupTransactionTag(
    transactionId = transactionId, tagId = tagId
)

fun BackupTransactionTag.toEntity() = TransactionTagCrossRef(
    transactionId = transactionId, tagId = tagId
)

fun AttachmentEntity.toBackup() = BackupAttachment(
    id = id, transactionId = transactionId, uri = uri, type = type, name = name
)

fun BackupAttachment.toEntity() = AttachmentEntity(
    id = id, transactionId = transactionId, uri = uri, type = type, name = name
)

fun BudgetEntity.toBackup() = BackupBudget(
    id = id, categoryId = categoryId, amount = amount, period = period,
    startDate = startDate, endDate = endDate, isActive = isActive
)

fun BackupBudget.toEntity() = BudgetEntity(
    id = id, categoryId = categoryId, amount = amount, period = period,
    startDate = startDate, endDate = endDate, isActive = isActive
)
