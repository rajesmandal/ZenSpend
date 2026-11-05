package com.android.zenspend.feature.dashboard.presentation.screen

import com.android.zenspend.core.ui.theme.AmountSmall
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.household.LedgerScope
import com.android.zenspend.core.ui.designsystem.component.AmountType
import com.android.zenspend.core.ui.designsystem.component.CategorySpendingBar
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenArrowBadge
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenSegmentedControl
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTransactionRow
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatWholeRupees
import com.android.zenspend.core.ui.util.greetingText
import com.android.zenspend.core.ui.util.relativeDateText
import com.android.zenspend.feature.budget.domain.model.BudgetProgress
import com.android.zenspend.feature.dashboard.domain.model.CategorySpending
import com.android.zenspend.feature.dashboard.domain.model.UpcomingEmiSummary
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiEvent
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiState
import com.android.zenspend.feature.dashboard.presentation.viewmodel.DashboardViewModel
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.transaction.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Where the dashboard can send the user. Grouped so the call-site stays readable. */
data class DashboardNavigation(
    val onAddTransaction: () -> Unit = {},
    val onTransactions: () -> Unit = {},
    val onRecurring: () -> Unit = {},
    val onBudgets: () -> Unit = {},
    val onWallets: () -> Unit = {},
    val onCreditCards: () -> Unit = {},
    val onEmis: () -> Unit = {},
    val onSmsDrafts: () -> Unit = {},
    val onAssistant: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onReports: () -> Unit = {}
)

@Composable
fun DashboardScreen(
    navigation: DashboardNavigation,
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
        navigation = navigation,
        ledgerScope = ledgerScope,
        onLedgerScopeChange = onLedgerScopeChange,
        showScopeToggle = showScopeToggle
    )
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    adController: com.android.zenspend.core.monetization.presentation.AdController? = null,
    onEvent: (DashboardUiEvent) -> Unit,
    navigation: DashboardNavigation,
    ledgerScope: LedgerScope = LedgerScope.PERSONAL,
    onLedgerScopeChange: (LedgerScope) -> Unit = {},
    showScopeToggle: Boolean = false
) {
    val isHousehold = ledgerScope == LedgerScope.HOUSEHOLD && showScopeToggle

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
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
                contentPadding = PaddingValues(
                    horizontal = Dimensions.ScreenPadding,
                    vertical = Dimensions.SpacingLarge
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item {
                    DashboardHeader(
                        title = stringResource(
                            if (isHousehold) R.string.dashboard_household_title else R.string.dashboard_title
                        ),
                        onSettings = navigation.onSettings
                    )
                }

                if (showScopeToggle) {
                    item {
                        ZenSegmentedControl(
                            options = listOf(
                                stringResource(R.string.scope_personal),
                                stringResource(R.string.scope_household)
                            ),
                            selectedIndex = if (ledgerScope == LedgerScope.PERSONAL) 0 else 1,
                            onSelected = { index ->
                                onLedgerScopeChange(
                                    if (index == 0) LedgerScope.PERSONAL else LedgerScope.HOUSEHOLD
                                )
                            }
                        )
                    }
                }

                item {
                    MonthSelector(
                        selectedMonth = state.selectedMonth,
                        onPrevious = { onEvent(DashboardUiEvent.PreviousMonth) },
                        onNext = { onEvent(DashboardUiEvent.NextMonth) }
                    )
                }

                // ---- Bento grid -------------------------------------------------
                item {
                    NetWorthTile(
                        total = formatWholeRupees(summary.netWorth.total),
                        liabilities = summary.netWorth.liabilities.takeIf { it > 0 }
                            ?.let { formatWholeRupees(it) },
                        income = formatWholeRupees(summary.monthlySummary.income),
                        spent = formatWholeRupees(summary.monthlySummary.expense)
                    )
                }

                item {
                    BudgetCard(
                        budget = summary.monthlyBudget,
                        daysLeft = daysLeftInMonth(state.selectedMonth),
                        onClick = navigation.onBudgets
                    )
                }

                item {
                    QuickChips(
                        emiBadge = summary.activeEmiCount,
                        navigation = navigation
                    )
                }

                // ---- Lists ------------------------------------------------------
                val upcoming = upcomingItems(summary.upcomingEmi, summary.upcomingRecurring)
                if (upcoming.isNotEmpty()) {
                    item {
                        UpcomingSection(items = upcoming, onViewAll = navigation.onRecurring)
                    }
                }

                if (summary.recentTransactions.isNotEmpty()) {
                    item {
                        ZenSectionHeader(
                            title = stringResource(R.string.dashboard_recent),
                            modifier = Modifier.padding(top = Dimensions.SpacingMedium),
                            actionLabel = stringResource(R.string.action_see_all),
                            onAction = navigation.onTransactions
                        )
                    }
                    items(summary.recentTransactions.take(Dimensions.HomeRecentCount)) { tx ->
                        RecentTransactionRow(tx = tx, onClick = navigation.onTransactions)
                    }
                }

                item { AdSlot(adController = adController) }
            }
        }
    }
}

// ---- Header -----------------------------------------------------------------

@Composable
private fun DashboardHeader(title: String, onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greetingText(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
        }
        HeaderIconButton(
            icon = Icons.Default.NotificationsNone,
            contentDescription = stringResource(R.string.action_notifications),
            onClick = { /* Notifications not available yet */ }
        )
        Spacer(modifier = Modifier.size(Dimensions.SpacingSmall))
        HeaderIconButton(
            icon = Icons.Default.Settings,
            contentDescription = stringResource(R.string.action_settings),
            onClick = onSettings
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(Dimensions.MinTouchTarget - 2.dp)
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant, PillShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Dimensions.IconSizeSmall)
        )
    }
}

/** ‹  November 2026  › — shared by Dashboard, Budgets and Insights. */
@Composable
fun MonthSelector(
    selectedMonth: Calendar,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.time)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.action_previous_month)
            )
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.action_next_month)
            )
        }
    }
}

// ---- Tiles ------------------------------------------------------------------

/** Equal-height row of half-width tiles. */
@Composable
private fun BentoRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.TileGap),
        content = content
    )
}

/**
 * Net worth card, kept minimal: the total, then assets / liabilities. This month's income and
 * spent sit in two capsules whose centre line is the card's bottom edge (half in, half out).
 */
@Composable
private fun NetWorthTile(
    total: String,
    liabilities: String?,
    income: String,
    spent: String
) {
    val coral = ZenSpendTheme.colors.tone(ZenTone.Coral).container
    val overhang = Dimensions.FlowCapsuleHeight / 2

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = overhang)) {
        ZenTile(
            modifier = Modifier.fillMaxWidth(),
            tone = ZenTone.Ink,
            contentPadding = PaddingValues(Dimensions.SpacingXLarge)
        ) {
            ZenTileCaption(stringResource(R.string.dashboard_net_worth))
            ZenTileValue(
                text = stringResource(R.string.amount_rupees, total),
                style = AmountHero
            )
            if (liabilities != null) {
                Spacer(Modifier.height(Dimensions.SpacingSmall))
                Text(
                    text = stringResource(R.string.dashboard_liabilities_line, liabilities),
                    style = MaterialTheme.typography.labelMedium,
                    color = coral
                )
            }
            // Room for the top half of the capsules that straddle the bottom edge.
            Spacer(Modifier.height(overhang + Dimensions.SpacingSmall))
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = overhang)
                .padding(horizontal = Dimensions.SpacingXLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            FlowCapsule(
                tone = ZenTone.Lime,
                icon = Icons.Default.ArrowDownward,
                caption = stringResource(R.string.dashboard_income),
                amount = income,
                modifier = Modifier.weight(1f)
            )
            FlowCapsule(
                tone = ZenTone.Coral,
                icon = Icons.Default.ArrowUpward,
                caption = stringResource(R.string.dashboard_spent),
                amount = spent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Pill with a ring in the screen background colour so it reads as cut out of the card edge. */
@Composable
private fun FlowCapsule(
    tone: ZenTone,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    caption: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    val colors = ZenSpendTheme.colors.tone(tone)
    Row(
        modifier = modifier
            .height(Dimensions.FlowCapsuleHeight)
            .border(Dimensions.FlowCapsuleRing, MaterialTheme.colorScheme.background, PillShape)
            .clip(PillShape)
            .background(colors.container)
            .padding(horizontal = Dimensions.SpacingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        Icon(icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(Dimensions.IconSizeSmall))
        Column {
            Text(caption, style = MaterialTheme.typography.labelSmall, color = colors.contentMuted, maxLines = 1)
            Text(
                text = stringResource(R.string.amount_rupees, amount),
                style = AmountSmall,
                color = colors.content,
                maxLines = 1
            )
        }
    }
}

/** Full-width budget card: what is left, a progress bar and what that means per day. */
@Composable
private fun BudgetCard(budget: BudgetProgress?, daysLeft: Int?, onClick: () -> Unit) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Lilac, onClick = onClick) {
        if (budget == null) {
            ZenTileCaption(stringResource(R.string.dashboard_budget_empty_title))
            Spacer(Modifier.height(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.dashboard_budget_empty_body),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val over = budget.isOverBudget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    ZenTileCaption(
                        stringResource(
                            if (over) R.string.dashboard_budget_over else R.string.dashboard_budget_left
                        )
                    )
                    ZenTileValue(
                        text = stringResource(
                            R.string.amount_rupees,
                            formatWholeRupees(kotlin.math.abs(budget.remainingAmount))
                        ),
                        style = AmountLarge
                    )
                }
                if (!over && daysLeft != null && budget.remainingAmount > 0) {
                    Text(
                        text = stringResource(
                            R.string.dashboard_budget_per_day,
                            formatWholeRupees(budget.remainingAmount / daysLeft)
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(Modifier.height(Dimensions.SpacingMedium))
            ZenProgressBar(
                progress = budget.progress,
                fillColor = if (over) ZenSpendTheme.colors.expense else androidx.compose.ui.graphics.Color.Unspecified
            )
        }
    }
}

/** One calm row of shortcuts instead of four big tiles. */
@Composable
private fun QuickChips(emiBadge: Int, navigation: DashboardNavigation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        QuickChip(Icons.Default.AutoAwesome, stringResource(R.string.dashboard_chip_ai), null, navigation.onAssistant)
        QuickChip(Icons.Default.Sms, stringResource(R.string.dashboard_chip_sms), null, navigation.onSmsDrafts)
        QuickChip(Icons.Default.ReceiptLong, stringResource(R.string.dashboard_emi_caption), emiBadge, navigation.onEmis)
        QuickChip(Icons.Default.CreditCard, stringResource(R.string.dashboard_quick_cards), null, navigation.onCreditCards)
    }
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badge: Int?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant, PillShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimensions.SpacingMedium - 3.dp, vertical = Dimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(Dimensions.IconSizeSmall))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        if (badge != null && badge > 0) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = Dimensions.SpacingSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/** Days left in the selected month, today included; null when that month is not the current one. */
private fun daysLeftInMonth(selected: Calendar): Int? {
    val now = Calendar.getInstance()
    if (now.get(Calendar.YEAR) != selected.get(Calendar.YEAR) ||
        now.get(Calendar.MONTH) != selected.get(Calendar.MONTH)
    ) return null
    return now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH) + 1
}

private data class UpcomingItem(
    val title: String,
    val amount: Long,
    val date: Long,
    val isEmi: Boolean
)

/** EMI + recurring merged, due within the next week, soonest first, capped for the home screen. */
private fun upcomingItems(
    emi: UpcomingEmiSummary?,
    recurring: List<RecurringTransaction>
): List<UpcomingItem> {
    val horizon = System.currentTimeMillis() + Dimensions.HomeUpcomingDays * DAY_MILLIS
    val all = listOfNotNull(emi?.let { UpcomingItem(it.name, it.amount, it.dueDate, true) }) +
        recurring.map { UpcomingItem(it.name, it.amount, it.nextExecutionDate, false) }
    return all.filter { it.date <= horizon }.sortedBy { it.date }.take(Dimensions.HomeUpcomingCount)
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

// ---- Sections ---------------------------------------------------------------

@Composable
private fun UpcomingSection(items: List<UpcomingItem>, onViewAll: () -> Unit) {
    Column {
        ZenSectionHeader(
            title = stringResource(R.string.dashboard_upcoming),
            modifier = Modifier.padding(top = Dimensions.SpacingMedium),
            actionLabel = stringResource(R.string.action_view_all),
            onAction = onViewAll
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
            items.forEach {
                ZenTransactionRow(
                    title = it.title,
                    subtitle = stringResource(
                        if (it.isEmi) R.string.dashboard_emi_due_soon else R.string.dashboard_recurring_on,
                        relativeDateText(it.date)
                    ),
                    amount = stringResource(R.string.amount_rupees, formatWholeRupees(it.amount)),
                    badgeTone = if (it.isEmi) ZenTone.Sky else ZenTone.Lilac,
                    onClick = onViewAll
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: Transaction, onClick: () -> Unit) {
    val isIncome = tx.type == TransactionType.INCOME || tx.type == TransactionType.REFUND
    val isExpense = tx.type == TransactionType.EXPENSE
    val formatted = formatWholeRupees(tx.amount)
    ZenTransactionRow(
        title = tx.title.ifBlank { stringResource(R.string.untitled) },
        subtitle = relativeDateText(tx.date),
        amount = when {
            isIncome -> stringResource(R.string.amount_rupees_income, formatted)
            isExpense -> stringResource(R.string.amount_rupees_expense, formatted)
            else -> stringResource(R.string.amount_rupees, formatted)
        },
        amountType = when {
            isIncome -> AmountType.INCOME
            isExpense -> AmountType.NEUTRAL
            else -> AmountType.TRANSFER
        },
        onClick = onClick
    )
}

@Composable
private fun AdSlot(adController: com.android.zenspend.core.monetization.presentation.AdController?) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current || adController == null) {
        Text(
            text = stringResource(R.string.ad_banner_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            textAlign = TextAlign.Center
        )
    } else {
        com.android.zenspend.core.monetization.presentation.components.BannerAd(
            adController = adController,
            unitId = com.android.zenspend.core.monetization.AdConfig.DASHBOARD_BANNER_ID,
            currentRoute = "dashboard"
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
            Text(
                text = stringResource(R.string.dashboard_empty_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.dashboard_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
