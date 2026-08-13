package com.android.zenspend.feature.dashboard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.dashboard.domain.model.DashboardSummary
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiEvent
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiState
import com.android.zenspend.feature.dashboard.presentation.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigateToRecurring: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onEvent = viewModel::handleEvent,
        onNavigateToRecurring = onNavigateToRecurring,
        onNavigateToTransactions = onNavigateToTransactions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("ZenSpend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(getGreeting(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.summary == null) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                // Month Selector
                item {
                    MonthSelector(
                        selectedMonth = state.selectedMonth,
                        onPrevious = { onEvent(DashboardUiEvent.PreviousMonth) },
                        onNext = { onEvent(DashboardUiEvent.NextMonth) }
                    )
                }

                state.summary?.let { summary ->
                    // Net Worth Card
                    item {
                        NetWorthCard(
                            total = formatAmount(summary.netWorth.total),
                            assets = formatAmount(summary.netWorth.assets),
                            liabilities = formatAmount(summary.netWorth.liabilities)
                        )
                    }

                    // Monthly Flow
                    item {
                        MonthlyFlowSection(
                            income = formatAmount(summary.monthlySummary.income),
                            expense = formatAmount(summary.monthlySummary.expense),
                            savings = formatAmount(summary.monthlySummary.savings)
                        )
                    }

                    // Monthly Budget
                    summary.monthlyBudget?.let { budget ->
                        item {
                            Text("Monthly Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        item {
                            com.android.zenspend.feature.budget.presentation.screen.BudgetOverviewCard(budget)
                        }
                    }

                    // Upcoming Transactions
                    if (summary.upcomingRecurring.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upcoming", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = onNavigateToRecurring) {
                                    Text("View all")
                                }
                            }
                        }
                        items(summary.upcomingRecurring) { rule ->
                            UpcomingRecurringItem(
                                name = rule.name,
                                amount = formatAmount(rule.amount),
                                date = formatDate(rule.nextExecutionDate),
                                isOverdue = rule.isOverdue
                            )
                        }
                    }

                    // Top Categories
                    if (summary.topCategories.isNotEmpty()) {
                        item {
                            Text("Top Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(summary.topCategories) { category ->
                            CategorySpendingBar(
                                category = category.categoryName,
                                amount = "₹${formatAmount(category.amount)}",
                                percentage = category.percentage
                            )
                        }
                    }

                    // Credit Card Summary
                    item {
                        CreditCardSummarySection(
                            outstanding = formatAmount(summary.creditCardSummary.totalOutstanding),
                            available = formatAmount(summary.creditCardSummary.totalAvailable),
                            cardCount = summary.creditCardSummary.activeCardsCount
                        )
                    }

                    // Recent Transactions
                    if (summary.recentTransactions.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = onNavigateToTransactions) {
                                    Text("View all")
                                }
                            }
                        }
                        items(summary.recentTransactions) { tx ->
                            TransactionItemMini(
                                title = tx.title,
                                amount = formatAmount(tx.amount),
                                type = tx.type.name
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonth: Calendar,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }
        Text(
            text = sdf.format(selectedMonth.time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun NetWorthCard(total: String, assets: String, liabilities: String) {
    SummaryCard(
        title = "Net Worth",
        amount = "₹$total",
        supportingText = "Assets: ₹$assets • Liabilities: ₹$liabilities",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MonthlyFlowSection(income: String, expense: String, savings: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
            FlowCard(label = "Income", amount = "₹$income", color = ZenSpendTheme.colors.income, modifier = Modifier.weight(1f))
            FlowCard(label = "Expense", amount = "₹$expense", color = ZenSpendTheme.colors.expense, modifier = Modifier.weight(1f))
        }
        ZenCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Dimensions.SpacingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Savings", style = MaterialTheme.typography.bodyMedium)
                Text("₹$savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ZenSpendTheme.colors.income)
            }
        }
    }
}

@Composable
fun FlowCard(label: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    ZenCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(amount, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CreditCardSummarySection(outstanding: String, available: String, cardCount: Int) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.SpacingLarge)) {
            Text("Credit Cards", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Outstanding", style = MaterialTheme.typography.labelSmall)
                    Text("₹$outstanding", style = MaterialTheme.typography.titleMedium, color = ZenSpendTheme.colors.expense)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available", style = MaterialTheme.typography.labelSmall)
                    Text("₹$available", style = MaterialTheme.typography.titleMedium, color = ZenSpendTheme.colors.income)
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Text("$cardCount Active Cards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun UpcomingRecurringItem(name: String, amount: String, date: String, isOverdue: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingXSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (isOverdue) "Overdue: $date" else "Due on $date",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverdue) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("₹$amount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionItemMini(title: String, amount: String, type: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingXSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text("₹$amount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

private fun formatAmount(amount: Long): String {
    return "%.2f".format(amount.toDouble() / 100.0)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
