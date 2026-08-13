package com.android.zenspend.feature.report.domain.model

data class FinancialOverview(
    val totalIncome: Long,
    val totalExpense: Long,
    val savings: Long,
    val savingsRate: Float,
    val comparison: PeriodComparison? = null
)

data class PeriodComparison(
    val incomeChange: Float, // Percentage
    val expenseChange: Float,
    val savingsChange: Float
)

data class CategoryReport(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float,
    val transactionCount: Int,
    val highestExpense: Long? = null
)

data class CreditCardReport(
    val cardId: Long,
    val cardName: String,
    val spending: Long,
    val payments: Long,
    val outstanding: Long,
    val utilization: Float
)

data class EMIReport(
    val activeEmiCount: Int,
    val monthlyBurden: Long,
    val totalRemainingObligation: Long
)

data class TrendPoint(
    val date: String,
    val incomeAmount: Long,
    val expenseAmount: Long
)

data class WalletActivityReport(
    val walletId: Long,
    val walletName: String,
    val incomeAmount: Long,
    val expenseAmount: Long,
    val netMovement: Long
)
