package com.android.zenspend.feature.backup.domain.model

import com.android.zenspend.core.database.model.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupEnvelope(
    val metadata: BackupMetadata,
    val data: BackupData,
    val checksum: String
)

@Serializable
data class BackupMetadata(
    val schemaVersion: Int,
    val appVersion: String,
    val createdAt: Long,
    val deviceName: String? = null
)

@Serializable
data class BackupData(
    val wallets: List<BackupWallet>,
    val categories: List<BackupCategory>,
    val creditCards: List<BackupCreditCard>,
    val emis: List<BackupEMI>,
    val recurringTransactions: List<BackupRecurringTransaction>,
    val tags: List<BackupTag>,
    val transactions: List<BackupTransaction>,
    val transactionTags: List<BackupTransactionTag>,
    val attachments: List<BackupAttachment>,
    val budgets: List<BackupBudget>,
    val smsDrafts: List<BackupSmsDraft>,
    val merchantPreferences: List<BackupMerchantPreference>
)

@Serializable
data class BackupWallet(
    val id: Long,
    val name: String,
    val type: WalletType,
    val openingBalance: Long,
    val currentBalance: Long,
    val currency: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val sync: BackupSyncMetadata = BackupSyncMetadata()
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType,
    val isActive: Boolean,
    val isDefault: Boolean
)

@Serializable
data class BackupCreditCard(
    val id: Long,
    val walletId: Long,
    val bankName: String,
    val lastFourDigits: String?,
    val creditLimit: Long,
    val billingCycleDate: Int,
    val paymentDueDate: Int,
    val active: Boolean
)

@Serializable
data class BackupEMI(
    val id: Long,
    val name: String,
    val walletId: Long?,
    val principalAmount: Long,
    val emiAmount: Long,
    val interestRate: Double?,
    val totalTenure: Int,
    val remainingInstallments: Int,
    val startDate: Long,
    val endDate: Long,
    val dueDay: Int,
    val status: EMIStatus,
    // Added with the EMI manager; defaults keep older backup files readable.
    val lenderType: com.android.zenspend.core.database.model.EMILenderType =
        com.android.zenspend.core.database.model.EMILenderType.CREDIT_CARD,
    val lenderName: String = "",
    val note: String? = null
)

@Serializable
data class BackupRecurringTransaction(
    val id: Long,
    val name: String,
    val amount: Long,
    val type: TransactionType,
    val recurrenceType: RecurrenceType,
    val sourceWalletId: Long?,
    val destinationWalletId: Long?,
    val categoryId: Long?,
    val startDate: Long,
    val lastExecutedDate: Long?,
    val nextExecutionDate: Long,
    val isActive: Boolean,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupTag(
    val id: Long,
    val name: String,
    val color: String?
)

@Serializable
data class BackupTransaction(
    val id: Long,
    val title: String,
    val amount: Long,
    val date: Long,
    val note: String?,
    val type: TransactionType,
    val status: TransactionStatus,
    val source: TransactionSource,
    val sourceWalletId: Long?,
    val destinationWalletId: Long?,
    val categoryId: Long?,
    val emiId: Long?,
    val recurringTransactionId: Long?,
    val recurringOccurrenceKey: String?,
    val originalTransactionId: Long?,
    val externalReference: String?,
    val originalMessage: String?,
    val isAutoDetected: Boolean,
    val isUserConfirmed: Boolean,
    val isExcludedFromReports: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val sync: BackupSyncMetadata = BackupSyncMetadata()
)

@Serializable
data class BackupTransactionTag(
    val transactionId: Long,
    val tagId: Long
)

@Serializable
data class BackupAttachment(
    val id: Long,
    val transactionId: Long,
    val uri: String,
    val type: String,
    val name: String?
)

@Serializable
data class BackupBudget(
    val id: Long,
    val categoryId: Long?,
    val amount: Long,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean
)

@Serializable
data class BackupSmsDraft(
    val id: Long,
    val amount: Long,
    val type: TransactionType,
    val date: Long,
    val merchant: String?,
    val accountLast4: String?,
    val sender: String,
    val fingerprint: String,
    val status: com.android.zenspend.feature.sms.data.entity.SmsDraftStatus,
    val createdAt: Long
)

@Serializable
data class BackupMerchantPreference(
    val normalizedMerchant: String,
    val categoryId: Long,
    val usageCount: Int,
    val lastUsedAt: Long
)
