package com.android.zenspend.core.database.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    REFUND,
    CREDIT_CARD_PAYMENT // Specific transfer type for CC bill settlement
}

@Serializable
enum class WalletType {
    CASH,
    BANK,
    UPI,
    CREDIT_CARD,
    DEBIT_CARD,
    DIGITAL_WALLET
}

@Serializable
enum class CategoryType {
    EXPENSE,
    INCOME,
    BOTH
}

@Serializable
enum class TransactionStatus {
    COMPLETED,
    PENDING,
    CANCELLED,
    FLAGGED
}

@Serializable
enum class TransactionSource {
    MANUAL,
    SMS,
    VOICE,
    RECURRING
}

@Serializable
enum class EMIStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class RecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

@Serializable
enum class BudgetPeriod {
    MONTHLY,
    WEEKLY,
    CUSTOM
}
