package com.android.zenspend.feature.report.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenCard
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ExpenseRed
import com.android.zenspend.core.ui.theme.IncomeGreen
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.dashboard.presentation.screen.MonthSelector
import com.android.zenspend.feature.report.presentation.component.CategoryReportItem
import com.android.zenspend.feature.report.presentation.component.ReportSummarySection
import com.android.zenspend.feature.report.presentation.component.TrendLineView
import com.android.zenspend.feature.report.presentation.contract.ReportUiEvent
import com.android.zenspend.feature.report.presentation.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Expenses", "Income", "Budgets", "Cards", "Accounts")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Insights", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = Dimensions.SpacingLarge) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (state.isLoading && state.overview == null) {
                LoadingState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimensions.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
                ) {
                    item {
                        MonthSelector(
                            selectedMonth = state.selectedMonth,
                            onPrevious = { viewModel.handleEvent(ReportUiEvent.PreviousMonth) },
                            onNext = { viewModel.handleEvent(ReportUiEvent.NextMonth) }
                        )
                    }

                    when (selectedTab) {
                        0 -> {
                            state.overview?.let { overview ->
                                item {
                                    ReportSummarySection(
                                        income = formatAmount(overview.totalIncome),
                                        expense = formatAmount(overview.totalExpense),
                                        savings = formatAmount(overview.savings),
                                        savingsRate = "%.1f".format(overview.savingsRate),
                                        comparison = overview.comparison
                                    )
                                }
                            }
                            
                            state.emiReport?.let { emi ->
                                item {
                                    ZenCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
                                            Text("EMI Burden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Monthly: ₹${formatAmount(emi.monthlyBurden)}", style = MaterialTheme.typography.bodyMedium)
                                                Text("Active: ${emi.activeEmiCount}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Text(
                                                text = "Total Obligation: ₹${formatAmount(emi.totalRemainingObligation)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
                                            )
                                        }
                                    }
                                }
                            }

                            if (state.trendPoints.isNotEmpty()) {
                                item {
                                    Text("Spending Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                item {
                                    TrendLineView(points = state.trendPoints)
                                }
                            } else if (state.overview != null) {
                                item {
                                    Text("No trend data available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        1 -> {
                            if (state.expenseCategories.isNotEmpty()) {
                                items(state.expenseCategories) { report ->
                                    CategoryReportItem(
                                        name = report.categoryName,
                                        amount = formatAmount(report.amount),
                                        percentage = report.percentage,
                                        transactionCount = report.transactionCount,
                                        highestExpense = report.highestExpense?.let { formatAmount(it) }
                                    )
                                }
                            } else {
                                item {
                                    Text("No expense data for this period", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        2 -> {
                            if (state.incomeCategories.isNotEmpty()) {
                                items(state.incomeCategories) { report ->
                                    CategoryReportItem(
                                        name = report.categoryName,
                                        amount = formatAmount(report.amount),
                                        percentage = report.percentage,
                                        transactionCount = report.transactionCount
                                    )
                                }
                            } else {
                                item {
                                    Text("No income data for this period", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        3 -> {
                            if (state.budgetReports.isNotEmpty()) {
                                items(state.budgetReports) { progress ->
                                    com.android.zenspend.feature.budget.presentation.screen.BudgetOverviewCard(progress = progress)
                                }
                            } else {
                                item {
                                    Text("No budgets set for this period", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        4 -> {
                            items(state.creditCards) { card ->
                                ZenCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
                                        Text(card.cardName, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Outstanding: ₹${formatAmount(card.outstanding)}", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                                            Text("Utilization: ${"%.1f".format(card.utilization)}%", style = MaterialTheme.typography.bodySmall)
                                        }
                                        LinearProgressIndicator(
                                            progress = { card.utilization / 100f },
                                            modifier = Modifier.fillMaxWidth().padding(top = Dimensions.SpacingXSmall),
                                            color = if (card.utilization > 80) ExpenseRed else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        5 -> {
                            items(state.walletActivity) { activity ->
                                ZenCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
                                        Text(activity.walletName, style = MaterialTheme.typography.titleMedium)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("In: ₹${formatAmount(activity.incomeAmount)}", color = IncomeGreen, style = MaterialTheme.typography.bodySmall)
                                            Text("Out: ₹${formatAmount(activity.expenseAmount)}", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Long): String {
    return "%.2f".format(amount.toDouble() / 100.0)
}
