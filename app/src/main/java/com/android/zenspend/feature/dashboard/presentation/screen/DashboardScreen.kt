package com.android.zenspend.feature.dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.household.LedgerScope
import com.android.zenspend.feature.dashboard.domain.model.DashboardSummary
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiEvent
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiState
import com.android.zenspend.feature.dashboard.presentation.viewmodel.DashboardViewModel
import com.android.zenspend.feature.transaction.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigateToRecurring: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    ledgerScope: LedgerScope = LedgerScope.PERSONAL,
    onLedgerScopeChange: (LedgerScope) -> Unit = {},
    showScopeToggle: Boolean = false,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        adController = viewModel.adController,
        onEvent = viewModel::handleEvent,
        onNavigateToRecurring = onNavigateToRecurring,
        onNavigateToTransactions = onNavigateToTransactions,
        ledgerScope = ledgerScope,
        onLedgerScopeChange = onLedgerScopeChange,
        showScopeToggle = showScopeToggle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    adController: com.android.zenspend.core.monetization.presentation.AdController? = null,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    ledgerScope: LedgerScope = LedgerScope.PERSONAL,
    onLedgerScopeChange: (LedgerScope) -> Unit = {},
    showScopeToggle: Boolean = false
) {
    Scaffold(
        topBar = {
            Column {
                if (showScopeToggle) {
                    ZenSegmentedControl(
                        options = listOf("Personal", "Our Household"),
                        selectedIndex = if (ledgerScope == LedgerScope.PERSONAL) 0 else 1,
                        onSelected = { index ->
                            onLedgerScopeChange(
                                if (index == 0) LedgerScope.PERSONAL else LedgerScope.HOUSEHOLD
                            )
                        },
                        modifier = Modifier.padding(
                            horizontal = Dimensions.ScreenPadding,
                            vertical = Dimensions.SpacingSmall
                        )
                    )
                }
                ZenTopAppBar(
                    title = if (ledgerScope == LedgerScope.HOUSEHOLD && showScopeToggle) "Our Household" else "Dashboard",
                    subtitle = getGreeting(),
                    actions = {
                        IconButton(onClick = { /* Notifications */ }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (state.isLoading && state.summary == null) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.summary == null) {
            EmptyDashboardState(modifier = Modifier.padding(padding))
        } else {
            val summary = state.summary
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                // Month Selector - Kept minimal
                item {
                    MonthSelector(
                        selectedMonth = state.selectedMonth,
                        onPrevious = { onEvent(DashboardUiEvent.PreviousMonth) },
                        onNext = { onEvent(DashboardUiEvent.NextMonth) }
                    )
                }

                // 2. NET WORTH / TOTAL BALANCE CARD
                item {
                    NetWorthSection(
                        total = formatAmount(summary.netWorth.total),
                        assets = formatAmount(summary.netWorth.assets),
                        liabilities = formatAmount(summary.netWorth.liabilities)
                    )
                }

                // 3. MONTHLY MONEY FLOW
                item {
                    MonthlyFlowSection(
                        income = formatAmount(summary.monthlySummary.income),
                        expense = formatAmount(summary.monthlySummary.expense),
                        savings = formatAmount(summary.monthlySummary.savings)
                    )
                }

                // 4. BUDGET STATUS
                summary.monthlyBudget?.let { budget ->
                    item {
                        BudgetSection(budget = budget)
                    }
                } ?: item {
                    SetBudgetPlaceholder()
                }

                // 5. SPENDING BREAKDOWN
                if (summary.topCategories.isNotEmpty()) {
                    item {
                        SpendingBreakdownSection(
                            categories = summary.topCategories.take(4),
                            onViewAll = { /* Navigate to reports */ }
                        )
                    }
                }

                // 6. UPCOMING
                val hasUpcoming = summary.upcomingRecurring.isNotEmpty() || summary.upcomingEmi != null
                if (hasUpcoming) {
                    item {
                        UpcomingSection(
                            recurring = summary.upcomingRecurring.take(2),
                            emi = summary.upcomingEmi,
                            onViewAll = onNavigateToRecurring
                        )
                    }
                }

                // 7. RECENT TRANSACTIONS
                if (summary.recentTransactions.isNotEmpty()) {
                    item {
                        RecentTransactionsSection(
                            transactions = summary.recentTransactions.take(5),
                            onViewAll = onNavigateToTransactions
                        )
                    }
                }

                item {
                    if (androidx.compose.ui.platform.LocalInspectionMode.current || adController == null) {
                        Text(
                            "Ad Banner Placeholder",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        com.android.zenspend.core.monetization.presentation.components.BannerAd(
                            adController = adController,
                            unitId = com.android.zenspend.core.monetization.AdConfig.DASHBOARD_BANNER_ID,
                            currentRoute = "dashboard"
                        )
                    }
                }
                
                // Add some spacing at the bottom for the FAB
                item { Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge)) }
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
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = sdf.format(selectedMonth.time),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NetWorthSection(total: String, assets: String, liabilities: String) {
    ZenSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Total Net Worth",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingXSmall))
        AmountText(
            amount = "₹$total",
            type = AmountType.NEUTRAL,
            style = com.android.zenspend.core.ui.theme.AmountHero
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Assets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AmountText(amount = "₹$assets", type = AmountType.INCOME)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Liabilities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AmountText(amount = "₹$liabilities", type = AmountType.EXPENSE)
            }
        }
    }
}

@Composable
fun MonthlyFlowSection(income: String, expense: String, savings: String) {
    ZenSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "This Month",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FlowItem("Income", "₹$income", AmountType.INCOME)
            FlowItem("Expenses", "₹$expense", AmountType.EXPENSE)
            FlowItem("Saved", "₹$savings", AmountType.INCOME)
        }
    }
}

@Composable
fun FlowItem(label: String, amount: String, type: AmountType) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AmountText(amount = amount, type = type)
    }
}

@Composable
fun BudgetSection(budget: com.android.zenspend.feature.budget.domain.model.BudgetProgress) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.SpacingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Budget", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    "₹${formatAmount(budget.spentAmount)} / ₹${formatAmount(budget.budget.amount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            LinearProgressIndicator(
                progress = { budget.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (budget.progress > 0.9f) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Text(
                text = "₹${formatAmount(budget.remainingAmount)} remaining",
                style = MaterialTheme.typography.labelSmall,
                color = if (budget.remainingAmount < 0) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SetBudgetPlaceholder() {
    ZenCard(modifier = Modifier.fillMaxWidth(), onClick = { /* Navigate to Budget */ }) {
        Row(
            modifier = Modifier.padding(Dimensions.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Set a monthly budget", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Track your spending habits better", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Set up", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpendingBreakdownSection(
    categories: List<com.android.zenspend.feature.dashboard.domain.model.CategorySpending>,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Spending", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) {
                Text("View all", style = MaterialTheme.typography.labelMedium)
            }
        }
        ZenCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                categories.forEach { category ->
                    CategorySpendingBar(
                        category = category.categoryName,
                        amount = "₹${formatAmount(category.amount)}",
                        percentage = category.percentage
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingSection(
    recurring: List<com.android.zenspend.feature.recurring.domain.model.RecurringTransaction>,
    emi: com.android.zenspend.feature.dashboard.domain.model.UpcomingEmiSummary?,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upcoming", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) {
                Text("View all", style = MaterialTheme.typography.labelMedium)
            }
        }
        ZenCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Dimensions.SpacingLarge), verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                emi?.let { 
                    UpcomingItem(it.name, formatAmount(it.amount), "Due soon", isEmi = true)
                }
                recurring.forEach { 
                    UpcomingItem(it.name, formatAmount(it.amount), formatDate(it.nextExecutionDate))
                }
            }
        }
    }
}

@Composable
fun UpcomingItem(title: String, amount: String, date: String, isEmi: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(if (isEmi) "EMI • $date" else "Recurring • $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("₹$amount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<Transaction>, onViewAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Transactions", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) {
                Text("View all", style = MaterialTheme.typography.labelMedium)
            }
        }
        ZenCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingSmall)) {
                transactions.forEachIndexed { index, tx ->
                    TransactionItemMini(tx)
                    if (index < transactions.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemMini(tx: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(formatDate(tx.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val prefix = if (tx.type == com.android.zenspend.core.database.model.TransactionType.INCOME) "+" else "-"
        val color = if (tx.type == com.android.zenspend.core.database.model.TransactionType.INCOME) ZenSpendTheme.colors.income else MaterialTheme.colorScheme.onSurface
        Text(
            text = "$prefix₹${formatAmount(tx.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyDashboardState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Dimensions.SpacingXXLarge)
        ) {
            Text("Start tracking your money", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Text(
                "Add your first income or expense to see your financial overview.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
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
    val doubleAmount = amount.toDouble() / 100.0
    return if (doubleAmount % 1.0 == 0.0) {
        "%,d".format(doubleAmount.toLong())
    } else {
        "%,.2f".format(doubleAmount)
    }
}

private fun formatDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return when {
        now.get(Calendar.DATE) == date.get(Calendar.DATE) -> "Today"
        now.get(Calendar.DATE) - 1 == date.get(Calendar.DATE) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
