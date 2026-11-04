package com.android.zenspend.feature.report.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import com.android.zenspend.core.ui.util.formatMinorUnits
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.AmountSmall
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.budget.presentation.screen.BudgetOverviewCard
import com.android.zenspend.feature.dashboard.presentation.screen.MonthSelector
import com.android.zenspend.feature.report.presentation.component.CategoryReportItem
import com.android.zenspend.feature.report.presentation.component.ReportSummarySection
import com.android.zenspend.feature.report.presentation.component.TrendLineView
import com.android.zenspend.feature.report.presentation.contract.ReportUiEvent
import com.android.zenspend.feature.report.presentation.viewmodel.ReportViewModel

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.insights_tab_overview),
        stringResource(R.string.insights_tab_expenses),
        stringResource(R.string.insights_tab_income),
        stringResource(R.string.insights_tab_budgets),
        stringResource(R.string.insights_tab_cards),
        stringResource(R.string.insights_tab_accounts)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ZenTopAppBar(title = stringResource(R.string.insights_title)) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ZenChipRow(
                options = tabs,
                selectedIndex = selectedTab,
                onSelected = { selectedTab = it },
                modifier = Modifier.padding(vertical = Dimensions.SpacingSmall)
            )

            if (state.isLoading && state.overview == null) {
                LoadingState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.ScreenPadding,
                        vertical = Dimensions.SpacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
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
                                    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Sky) {
                                        ZenTileCaption(stringResource(R.string.insights_emi_burden))
                                        ZenTileValue(
                                            text = stringResource(
                                                R.string.amount_rupees,
                                                formatAmount(emi.monthlyBurden)
                                            ),
                                            style = AmountMedium
                                        )
                                        Spacer(Modifier.height(Dimensions.SpacingSmall))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            ZenTileCaption(
                                                "${stringResource(R.string.insights_emi_active)}: ${emi.activeEmiCount}"
                                            )
                                            ZenTileCaption(
                                                stringResource(
                                                    R.string.insights_emi_total,
                                                    formatAmount(emi.totalRemainingObligation)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            if (state.trendPoints.isNotEmpty()) {
                                item {
                                    ZenSectionHeader(
                                        title = stringResource(R.string.insights_spending_trend),
                                        modifier = Modifier.padding(top = Dimensions.SpacingMedium)
                                    )
                                }
                                item { TrendLineView(points = state.trendPoints) }
                            } else if (state.overview != null) {
                                item {
                                    EmptyHint(stringResource(R.string.insights_no_trend))
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
                                item { EmptyHint(stringResource(R.string.insights_no_expenses)) }
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
                                item { EmptyHint(stringResource(R.string.insights_no_income)) }
                            }
                        }

                        3 -> {
                            if (state.budgetReports.isNotEmpty()) {
                                items(state.budgetReports) { progress ->
                                    BudgetOverviewCard(progress = progress)
                                }
                            } else {
                                item { EmptyHint(stringResource(R.string.insights_no_budgets)) }
                            }
                        }

                        4 -> {
                            items(state.creditCards) { card ->
                                val expense = ZenSpendTheme.colors.expense
                                ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Leaf) {
                                    Text(
                                        text = card.cardName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(Dimensions.SpacingSmall))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            ZenTileCaption(stringResource(R.string.insights_card_outstanding))
                                            ZenTileValue(
                                                text = stringResource(
                                                    R.string.amount_rupees,
                                                    formatAmount(card.outstanding)
                                                ),
                                                style = AmountSmall
                                            )
                                        }
                                        Column {
                                            ZenTileCaption(stringResource(R.string.insights_card_utilization))
                                            ZenTileValue(
                                                text = "${"%.1f".format(card.utilization)}%",
                                                style = AmountSmall
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(Dimensions.SpacingMedium))
                                    ZenProgressBar(
                                        progress = card.utilization / 100f,
                                        fillColor = if (card.utilization > UTILIZATION_ALERT_PERCENT) {
                                            expense
                                        } else {
                                            androidx.compose.ui.graphics.Color.Unspecified
                                        }
                                    )
                                }
                            }
                        }

                        5 -> {
                            items(state.walletActivity) { activity ->
                                ZenTile(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = activity.walletName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(Dimensions.SpacingSmall))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        ZenTileValue(
                                            text = "${stringResource(R.string.insights_wallet_in)}: " +
                                                stringResource(
                                                    R.string.amount_rupees,
                                                    formatAmount(activity.incomeAmount)
                                                ),
                                            style = AmountSmall,
                                            color = ZenSpendTheme.colors.income
                                        )
                                        ZenTileValue(
                                            text = "${stringResource(R.string.insights_wallet_out)}: " +
                                                stringResource(
                                                    R.string.amount_rupees,
                                                    formatAmount(activity.expenseAmount)
                                                ),
                                            style = AmountSmall,
                                            color = ZenSpendTheme.colors.expense
                                        )
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

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Credit utilisation above this percentage is shown in the alert colour. */
private const val UTILIZATION_ALERT_PERCENT = 80

private fun formatAmount(amount: Long): String {
    return formatMinorUnits(amount)
}
