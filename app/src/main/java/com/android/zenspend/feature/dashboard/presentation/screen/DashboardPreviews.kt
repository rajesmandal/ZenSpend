package com.android.zenspend.feature.dashboard.presentation.screen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.dashboard.domain.model.*
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiState
import com.android.zenspend.feature.transaction.domain.model.Transaction
import java.util.Calendar

@Preview(showSystemUi = true, name = "Full Dashboard")
@Composable
fun DashboardPreview() {
    val summary = DashboardSummary(
        netWorth = NetWorthSummary(total = 43250000, assets = 50000000, liabilities = 6750000),
        monthlySummary = MonthlyFlowSummary(income = 7500000, expense = 4250000, savings = 3250000),
        topCategories = listOf(
            CategorySpending(1, "Food", 1250000, 0.4f),
            CategorySpending(2, "Shopping", 820000, 0.25f),
            CategorySpending(3, "Transport", 450000, 0.15f),
            CategorySpending(4, "Bills", 670000, 0.2f)
        ),
        creditCardSummary = CreditCardDashboardSummary(
            totalOutstanding = 4250000,
            totalAvailable = 15750000,
            activeCardsCount = 2
        ),
        upcomingEmi = UpcomingEmiSummary("iPhone 15", 500000, System.currentTimeMillis()),
        monthlyBudget = com.android.zenspend.feature.budget.domain.model.BudgetProgress(
            budget = com.android.zenspend.feature.budget.domain.model.Budget(amount = 5000000, categoryId = null, startDate = 0, endDate = 0),
            spentAmount = 3200000
        ),
        upcomingRecurring = emptyList(),
        recentTransactions = listOf(
            Transaction(title = "Starbucks", amount = 45000, date = System.currentTimeMillis(), type = TransactionType.EXPENSE, sourceWalletId = 1, note = null),
            Transaction(title = "Salary", amount = 5000000, date = System.currentTimeMillis(), type = TransactionType.INCOME, sourceWalletId = 2, note = null)
        )
    )

    ZenSpendTheme {
        Surface {
            DashboardContent(
                state = DashboardUiState(summary = summary),
                onEvent = {},
                onNavigateToRecurring = {},
                onNavigateToTransactions = {}
            )
        }
    }
}

@Preview(showSystemUi = true, name = "Empty Dashboard")
@Composable
fun EmptyDashboardPreview() {
    ZenSpendTheme {
        Surface {
            DashboardContent(
                state = DashboardUiState(summary = null),
                onEvent = {},
                onNavigateToRecurring = {},
                onNavigateToTransactions = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Month Selector")
@Composable
fun MonthSelectorPreview() {
    ZenSpendTheme {
        MonthSelector(selectedMonth = Calendar.getInstance(), onPrevious = {}, onNext = {})
    }
}
