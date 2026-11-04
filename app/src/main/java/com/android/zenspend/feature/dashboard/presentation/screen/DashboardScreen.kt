package com.android.zenspend.feature.dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.android.zenspend.core.ui.util.formatMinorUnits
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
                        total = formatMinorUnits(summary.netWorth.total),
                        assets = formatMinorUnits(summary.netWorth.assets),
                        liabilities = formatMinorUnits(summary.netWorth.liabilities),
                        navigation = navigation
                    )
                }

                item {
                    BentoRow {
                        FlowTile(
                            tone = ZenTone.Lime,
                            caption = stringResource(R.string.dashboard_income),
                            amount = formatMinorUnits(summary.monthlySummary.income),
                            onClick = navigation.onReports
                        )
                        FlowTile(
                            tone = ZenTone.Coral,
                            caption = stringResource(R.string.dashboard_spent),
                            amount = formatMinorUnits(summary.monthlySummary.expense),
                            onClick = navigation.onReports
                        )
                    }
                }

                item {
                    BentoRow {
                        FlowTile(
                            tone = ZenTone.Mint,
                            caption = stringResource(R.string.dashboard_saved),
                            amount = formatMinorUnits(summary.monthlySummary.savings),
                            onClick = navigation.onReports
                        )
                        BudgetTile(budget = summary.monthlyBudget, onClick = navigation.onBudgets)
                    }
                }

                item {
                    BentoRow {
                        ShortcutTile(
                            tone = ZenTone.Sky,
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                            caption = stringResource(R.string.dashboard_ai_caption),
                            body = stringResource(R.string.dashboard_ai_body),
                            onClick = navigation.onAssistant
                        )
                        ShortcutTile(
                            tone = ZenTone.Sun,
                            icon = { Icon(Icons.Default.Sms, contentDescription = null) },
                            caption = stringResource(R.string.dashboard_sms_caption),
                            body = stringResource(R.string.dashboard_sms_body),
                            onClick = navigation.onSmsDrafts
                        )
                    }
                }

                item {
                    BentoRow {
                        ShortcutTile(
                            tone = ZenTone.Lilac,
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                            caption = stringResource(R.string.dashboard_emi_caption),
                            body = if (summary.activeEmiCount > 0) {
                                stringResource(R.string.dashboard_emi_summary, formatMinorUnits(summary.emiMonthlyTotal))
                            } else {
                                stringResource(R.string.dashboard_emi_empty)
                            },
                            onClick = navigation.onEmis
                        )
                        ShortcutTile(
                            tone = ZenTone.Coral,
                            icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            caption = stringResource(R.string.dashboard_cards_caption),
                            body = stringResource(R.string.dashboard_cards_body),
                            onClick = navigation.onCreditCards
                        )
                    }
                }

                // ---- Lists ------------------------------------------------------
                if (summary.topCategories.isNotEmpty()) {
                    item {
                        TopSpendingSection(
                            categories = summary.topCategories.take(4),
                            onViewAll = navigation.onReports
                        )
                    }
                }

                val hasUpcoming = summary.upcomingRecurring.isNotEmpty() || summary.upcomingEmi != null
                if (hasUpcoming) {
                    item {
                        UpcomingSection(
                            recurring = summary.upcomingRecurring.take(2),
                            emi = summary.upcomingEmi,
                            onViewAll = navigation.onRecurring
                        )
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
                    items(summary.recentTransactions.take(5)) { tx ->
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

@Composable
private fun NetWorthTile(
    total: String,
    assets: String,
    liabilities: String,
    navigation: DashboardNavigation
) {
    val lime = ZenSpendTheme.colors.tone(ZenTone.Lime).container
    val coral = ZenSpendTheme.colors.tone(ZenTone.Coral).container

    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge - 2.dp)
    ) {
        ZenTileCaption(stringResource(R.string.dashboard_net_worth))
        Spacer(Modifier.height(Dimensions.SpacingXSmall))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, total),
            style = AmountHero
        )
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXLarge)) {
            Column {
                ZenTileCaption(stringResource(R.string.dashboard_assets))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, assets),
                    style = AmountMedium,
                    color = lime
                )
            }
            Column {
                ZenTileCaption(stringResource(R.string.dashboard_liabilities))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, liabilities),
                    style = AmountMedium,
                    color = coral
                )
            }
        }
        Spacer(Modifier.height(Dimensions.SpacingXLarge - 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
            HeroAction(
                label = stringResource(R.string.action_add),
                highlighted = true,
                onClick = navigation.onAddTransaction,
                modifier = Modifier.weight(1f)
            )
            HeroAction(
                label = stringResource(R.string.dashboard_quick_wallets),
                onClick = navigation.onWallets,
                modifier = Modifier.weight(1f)
            )
            HeroAction(
                label = stringResource(R.string.dashboard_quick_cards),
                onClick = navigation.onCreditCards,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val lime = ZenSpendTheme.colors.tone(ZenTone.Lime)
    val ink = ZenSpendTheme.colors.tone(ZenTone.Ink)
    Box(
        modifier = modifier
            .clip(PillShape)
            .background(if (highlighted) lime.container else ink.overlay)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Dimensions.SpacingMedium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (highlighted) lime.content else ink.content
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.FlowTile(
    tone: ZenTone,
    caption: String,
    amount: String,
    onClick: () -> Unit
) {
    ZenTile(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        tone = tone,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            ZenTileCaption(caption)
            ZenArrowBadge()
        }
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, amount),
            style = AmountLarge
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BudgetTile(
    budget: BudgetProgress?,
    onClick: () -> Unit
) {
    ZenTile(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        tone = ZenTone.Lilac,
        onClick = onClick
    ) {
        if (budget == null) {
            ZenTileCaption(stringResource(R.string.dashboard_budget_empty_title))
            Spacer(Modifier.height(Dimensions.SpacingSmall))
            Text(
                text = stringResource(R.string.dashboard_budget_empty_body),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val over = budget.isOverBudget
            ZenTileCaption(
                stringResource(
                    if (over) R.string.dashboard_budget_over else R.string.dashboard_budget_left
                )
            )
            ZenTileValue(
                text = stringResource(
                    R.string.amount_rupees,
                    formatMinorUnits(kotlin.math.abs(budget.remainingAmount))
                ),
                style = AmountLarge
            )
            Spacer(Modifier.height(Dimensions.SpacingMedium))
            ZenProgressBar(
                progress = budget.progress,
                fillColor = if (over) ZenSpendTheme.colors.expense else androidx.compose.ui.graphics.Color.Unspecified
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ShortcutTile(
    tone: ZenTone,
    icon: @Composable () -> Unit,
    caption: String,
    body: String,
    onClick: () -> Unit
) {
    ZenTile(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        tone = tone,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            icon()
            ZenArrowBadge()
        }
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenTileCaption(caption)
        Text(
            text = body,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

// ---- Sections ---------------------------------------------------------------

@Composable
private fun TopSpendingSection(categories: List<CategorySpending>, onViewAll: () -> Unit) {
    Column {
        ZenSectionHeader(
            title = stringResource(R.string.dashboard_top_spending),
            modifier = Modifier.padding(top = Dimensions.SpacingMedium),
            actionLabel = stringResource(R.string.action_view_all),
            onAction = onViewAll
        )
        ZenTile(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)) {
                categories.forEach { category ->
                    CategorySpendingBar(
                        category = category.categoryName,
                        amount = stringResource(R.string.amount_rupees, formatMinorUnits(category.amount)),
                        percentage = category.percentage
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingSection(
    recurring: List<RecurringTransaction>,
    emi: UpcomingEmiSummary?,
    onViewAll: () -> Unit
) {
    Column {
        ZenSectionHeader(
            title = stringResource(R.string.dashboard_upcoming),
            modifier = Modifier.padding(top = Dimensions.SpacingMedium),
            actionLabel = stringResource(R.string.action_view_all),
            onAction = onViewAll
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
            emi?.let {
                ZenTransactionRow(
                    title = it.name,
                    subtitle = stringResource(R.string.dashboard_emi_due_soon, relativeDateText(it.dueDate)),
                    amount = stringResource(R.string.amount_rupees, formatMinorUnits(it.amount)),
                    badgeTone = ZenTone.Sky,
                    onClick = onViewAll
                )
            }
            recurring.forEach {
                ZenTransactionRow(
                    title = it.name,
                    subtitle = stringResource(
                        R.string.dashboard_recurring_on,
                        relativeDateText(it.nextExecutionDate)
                    ),
                    amount = stringResource(R.string.amount_rupees, formatMinorUnits(it.amount)),
                    badgeTone = ZenTone.Lilac,
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
    val formatted = formatMinorUnits(tx.amount)
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
