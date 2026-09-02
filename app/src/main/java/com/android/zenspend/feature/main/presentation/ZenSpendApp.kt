package com.android.zenspend.feature.main.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.zenspend.R
import com.android.zenspend.core.navigation.Destination
import com.android.zenspend.core.navigation.NavEvent
import com.android.zenspend.core.navigation.NavigationChrome
import com.android.zenspend.core.navigation.Navigator
import com.android.zenspend.core.sync.domain.model.SyncStatus
import com.android.zenspend.core.ui.designsystem.component.ZenNavItem
import com.android.zenspend.core.ui.designsystem.component.ZenNavigationBar
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.auth.presentation.screen.LoginScreen
import com.android.zenspend.feature.household.presentation.screen.SharedCashbookScreen
import com.android.zenspend.feature.backup.presentation.screen.BackupScreen
import com.android.zenspend.feature.ai.presentation.screen.AiAssistantScreen
import com.android.zenspend.feature.premium.presentation.screen.PremiumScreen
import com.android.zenspend.feature.sms.presentation.screen.SmsDraftListScreen
import com.android.zenspend.feature.sms.presentation.screen.SmsDraftReviewScreen
import com.android.zenspend.feature.settings.presentation.screen.SettingsScreen
import com.android.zenspend.feature.dashboard.presentation.screen.DashboardScreen
import com.android.zenspend.feature.budget.presentation.screen.AddEditBudgetScreen
import com.android.zenspend.feature.budget.presentation.screen.BudgetListScreen
import com.android.zenspend.feature.creditcard.presentation.screen.AddEditCreditCardScreen
import com.android.zenspend.feature.creditcard.presentation.screen.CreditCardDetailsScreen
import com.android.zenspend.feature.creditcard.presentation.screen.CreditCardListScreen
import com.android.zenspend.feature.emi.presentation.screen.AddEditEMIScreen
import com.android.zenspend.feature.emi.presentation.screen.EMIListScreen
import com.android.zenspend.feature.recurring.presentation.screen.AddEditRecurringTransactionScreen
import com.android.zenspend.feature.recurring.presentation.screen.RecurringTransactionDetailsScreen
import com.android.zenspend.feature.recurring.presentation.screen.RecurringTransactionListScreen
import com.android.zenspend.feature.report.presentation.screen.ReportScreen
import com.android.zenspend.feature.transaction.presentation.screen.TransactionAddEditScreen
import com.android.zenspend.feature.transaction.presentation.screen.TransactionDetailsScreen
import com.android.zenspend.feature.transaction.presentation.screen.TransactionListScreen
import com.android.zenspend.feature.wallet.presentation.screen.WalletAddEditScreen
import com.android.zenspend.feature.wallet.presentation.screen.WalletDetailsScreen
import com.android.zenspend.feature.wallet.presentation.screen.WalletListScreen
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

private val bottomNavItems = listOf(
    ZenNavItem("Home", R.drawable.ic_nav_home),
    ZenNavItem("History", R.drawable.ic_nav_history),
    ZenNavItem("Wallets", R.drawable.ic_nav_wallets),
    ZenNavItem("Insights", R.drawable.ic_nav_insights),
    ZenNavItem("More", R.drawable.ic_nav_more)
)

private val bottomNavRoutes = listOf(
    Destination.Dashboard.route,
    Destination.Transactions.route,
    Destination.Wallets.route,
    Destination.Reports.route,
    Destination.Settings.route
)

@Composable
fun ZenSpendApp(
    navigator: Navigator,
    shellViewModel: MainShellViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeHouseholdId by shellViewModel.scopeManager.activeHouseholdId.collectAsState()
    val ledgerScope by shellViewModel.scopeManager.scope.collectAsState()
    val syncStatus by shellViewModel.syncStatusManager.status.collectAsState(initial = SyncStatus.SYNCED)

    val selectedTabIndex = bottomNavRoutes.indexOfFirst { route ->
        navBackStackEntry?.destination?.hierarchy?.any { it.route == route } == true
    }.coerceAtLeast(0)

    LaunchedEffect(Unit) {
        navigator.navEvents.collectLatest { event ->
            when (event) {
                is NavEvent.To -> navController.navigate(event.destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                is NavEvent.Up -> navController.navigateUp()
            }
        }
    }

    val showBottomBar = NavigationChrome.showsBottomBar(currentRoute)
    val showFab = NavigationChrome.showsFab(currentRoute)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if (activeHouseholdId != null) {
                        val syncMessage = when (syncStatus) {
                            SyncStatus.SYNCED -> "Synced"
                            SyncStatus.SYNCING -> "Syncing..."
                            SyncStatus.OFFLINE -> "Offline — saved on this device"
                            SyncStatus.FAILED -> "Sync failed — will retry"
                        }
                        ZenStatusBanner(
                            message = syncMessage,
                            isError = syncStatus == SyncStatus.FAILED,
                            modifier = Modifier.padding(
                                horizontal = Dimensions.ScreenPadding,
                                vertical = Dimensions.SpacingSmall
                            )
                        )
                    }
                    ZenNavigationBar(
                        selectedIndex = selectedTabIndex,
                        items = bottomNavItems,
                        onItemSelected = { index ->
                            navController.navigate(bottomNavRoutes[index]) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = {
                        navigator.navigate(NavigationChrome.fabDestination(currentRoute))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(NavigationChrome.fabLabel(currentRoute)) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Dashboard.route) {
                DashboardScreen(
                    onNavigateToRecurring = { navigator.navigate(Destination.RecurringTransactions) },
                    onNavigateToTransactions = { navigator.navigate(Destination.Transactions) },
                    ledgerScope = ledgerScope,
                    onLedgerScopeChange = { scope -> shellViewModel.scopeManager.setScope(scope) },
                    showScopeToggle = activeHouseholdId != null
                )
            }
            composable(Destination.Transactions.route) {
                TransactionListScreen(
                    onNavigateToAdd = { navigator.navigate(Destination.AddTransaction) },
                    onNavigateToDetails = { id -> navigator.navigate(Destination.TransactionDetails(id)) }
                )
            }
            composable(Destination.Wallets.route) {
                WalletListScreen(
                    onNavigateToAdd = { navigator.navigate(Destination.AddWallet) },
                    onNavigateToDetails = { id -> navigator.navigate(Destination.WalletDetails(id)) }
                )
            }
            composable(Destination.AddWallet.route) {
                WalletAddEditScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.WalletDetails.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("walletId")?.toLongOrNull() ?: 0L
                WalletDetailsScreen(
                    walletId = id,
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToEdit = { navigator.navigate(Destination.EditWallet(it)) }
                )
            }
            composable(Destination.EditWallet.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("walletId")?.toLongOrNull() ?: 0L
                WalletAddEditScreen(onNavigateBack = { navigator.navigateUp() }, walletId = id)
            }
            composable(Destination.CreditCards.route) {
                CreditCardListScreen(
                    onNavigateToAdd = { navigator.navigate(Destination.AddCreditCard) },
                    onNavigateToDetails = { id -> navigator.navigate(Destination.CreditCardDetails(id)) }
                )
            }
            composable(Destination.AddCreditCard.route) {
                AddEditCreditCardScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.CreditCardDetails.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("cardId")?.toLongOrNull() ?: 0L
                CreditCardDetailsScreen(
                    cardId = id,
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToAddEmi = { cardId -> navigator.navigate(Destination.AddEmi(cardId)) }
                )
            }
            composable(Destination.AddEmi.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("creditCardId")?.toLongOrNull() ?: 0L
                AddEditEMIScreen(
                    creditCardId = id,
                    onNavigateBack = { navigator.navigateUp() }
                )
            }
            composable(Destination.Emis.route) {
                EMIListScreen(onNavigateToAdd = { navigator.navigate(Destination.CreditCards) })
            }
            composable(Destination.Budgets.route) {
                BudgetListScreen(onNavigateToAdd = { navigator.navigate(Destination.AddBudget) })
            }
            composable(Destination.AddBudget.route) {
                AddEditBudgetScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.BudgetDetails.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("budgetId")?.toLongOrNull() ?: 0L
                com.android.zenspend.feature.budget.presentation.screen.BudgetDetailsScreen(
                    budgetId = id,
                    onNavigateBack = { navigator.navigateUp() }
                )
            }
            composable(Destination.RecurringTransactions.route) {
                RecurringTransactionListScreen(
                    onNavigateToAdd = { navigator.navigate(Destination.AddRecurringTransaction) },
                    onNavigateToDetails = { id -> navigator.navigate(Destination.RecurringDetails(id)) },
                    onNavigateBack = { navigator.navigateUp() }
                )
            }
            composable(Destination.AddRecurringTransaction.route) {
                AddEditRecurringTransactionScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.RecurringDetails.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("recurringId")?.toLongOrNull() ?: 0L
                RecurringTransactionDetailsScreen(
                    recurringId = id,
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToEdit = { navigator.navigate(Destination.EditRecurring(it)) }
                )
            }
            composable(Destination.EditRecurring.ROUTE) {
                AddEditRecurringTransactionScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.AddTransaction.route) {
                TransactionAddEditScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.TransactionDetails.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
                TransactionDetailsScreen(
                    transactionId = id,
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToEdit = { navigator.navigate(Destination.EditTransaction(it)) }
                )
            }
            composable(Destination.EditTransaction.ROUTE) {
                TransactionAddEditScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.Reports.route) { ReportScreen() }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onNavigateToBackup = { navigator.navigate(Destination.Backup) },
                    onNavigateToSmsDrafts = { navigator.navigate(Destination.SmsDrafts) },
                    onNavigateToAiAssistant = { navigator.navigate(Destination.AiAssistant) },
                    onNavigateToPremium = { navigator.navigate(Destination.Premium) },
                    onNavigateToSharedCashbook = { navigator.navigate(Destination.SharedCashbook) },
                    onNavigateToCreditCards = { navigator.navigate(Destination.CreditCards) },
                    onNavigateToAbout = { }
                )
            }
            composable(Destination.Login.route) {
                LoginScreen(
                    onAuthenticated = { navigator.navigateUp() },
                    onNavigateBack = { navigator.navigateUp() }
                )
            }
            composable(Destination.SharedCashbook.route) {
                SharedCashbookScreen(
                    onNavigateToLogin = { navigator.navigate(Destination.Login) },
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToPremium = { navigator.navigate(Destination.Premium) }
                )
            }
            composable(Destination.Backup.route) {
                BackupScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.AiAssistant.route) {
                AiAssistantScreen(
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToPremium = { navigator.navigate(Destination.Premium) }
                )
            }
            composable(Destination.SmsDrafts.route) {
                SmsDraftListScreen(
                    onNavigateBack = { navigator.navigateUp() },
                    onNavigateToReview = { id -> navigator.navigate(Destination.SmsReview(id)) }
                )
            }
            composable(Destination.SmsReview.ROUTE) {
                SmsDraftReviewScreen(
                    onNavigateBack = { navigator.navigateUp() },
                    onSuccess = { navigator.navigateUp() }
                )
            }
            composable(Destination.Premium.route) {
                PremiumScreen(onNavigateBack = { navigator.navigateUp() })
            }
        }
    }
}
