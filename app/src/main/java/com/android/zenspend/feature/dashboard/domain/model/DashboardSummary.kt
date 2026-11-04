package com.android.zenspend.feature.dashboard.domain.model

import com.android.zenspend.feature.transaction.domain.model.Transaction

data class DashboardSummary(
    val netWorth: NetWorthSummary,
    val monthlySummary: MonthlyFlowSummary,
    val topCategories: List<CategorySpending>,
    val creditCardSummary: CreditCardDashboardSummary,
    val upcomingEmi: UpcomingEmiSummary?,
    val activeEmiCount: Int = 0,
    val emiMonthlyTotal: Long = 0,
    val monthlyBudget: com.android.zenspend.feature.budget.domain.model.BudgetProgress?,
    val upcomingRecurring: List<com.android.zenspend.feature.recurring.domain.model.RecurringTransaction> = emptyList(),
    val recentTransactions: List<Transaction>
)

data class NetWorthSummary(
    val total: Long,
    val assets: Long,
    val liabilities: Long
)

data class MonthlyFlowSummary(
    val income: Long,
    val expense: Long,
    val savings: Long
)

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float
)

data class CreditCardDashboardSummary(
    val totalOutstanding: Long,
    val totalAvailable: Long,
    val activeCardsCount: Int
)

data class UpcomingEmiSummary(
    val name: String,
    val amount: Long,
    val dueDate: Long
)
