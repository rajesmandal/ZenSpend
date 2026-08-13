package com.android.zenspend.feature.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.zenspend.core.navigation.Destination
import com.android.zenspend.core.navigation.NavEvent
import com.android.zenspend.core.navigation.Navigator
import com.android.zenspend.feature.backup.presentation.screen.BackupScreen
import com.android.zenspend.feature.ai.presentation.screen.AiAssistantScreen
import com.android.zenspend.feature.sms.presentation.screen.SmsDraftListScreen
import com.android.zenspend.feature.sms.presentation.screen.SmsDraftReviewScreen
import com.android.zenspend.feature.settings.presentation.screen.SettingsScreen
import com.android.zenspend.feature.dashboard.presentation.screen.DashboardScreen
import com.android.zenspend.feature.budget.presentation.screen.AddEditBudgetScreen
import com.android.zenspend.feature.budget.presentation.screen.BudgetListScreen
import com.android.zenspend.feature.creditcard.presentation.screen.AddEditCreditCardScreen
import com.android.zenspend.feature.creditcard.presentation.screen.CreditCardListScreen
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
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(Destination.Dashboard.route, "Home", Icons.Default.Dashboard)
    object Transactions : BottomNavItem(Destination.Transactions.route, "History", Icons.Default.History)
    object Wallets : BottomNavItem(Destination.Wallets.route, "Wallets", Icons.Default.Wallet)
    object Reports : BottomNavItem(Destination.Reports.route, "Insights", Icons.Default.PieChart)
    object Settings : BottomNavItem(Destination.Settings.route, "More", Icons.Default.Settings)
}

@Composable
fun ZenSpendApp(
    navigator: Navigator
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem.Dashboard,
                    BottomNavItem.Transactions,
                    BottomNavItem.Wallets,
                    BottomNavItem.Reports,
                    BottomNavItem.Settings
                )
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
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
            FloatingActionButton(
                onClick = { navigator.navigate(Destination.AddTransaction) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
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
                    onNavigateToTransactions = { navigator.navigate(Destination.Transactions) }
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
                WalletAddEditScreen(
                    onNavigateBack = { navigator.navigateUp() },
                    walletId = id
                )
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

            composable(Destination.Emis.route) {
                EMIListScreen(onNavigateToAdd = { navigator.navigate(Destination.AddEmi) })
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
            composable(Destination.EditRecurring.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("recurringId")?.toLongOrNull() ?: 0L
                AddEditRecurringTransactionScreen(
                    onNavigateBack = { navigator.navigateUp() }
                )
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
            composable(Destination.EditTransaction.ROUTE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
                TransactionAddEditScreen(onNavigateBack = { navigator.navigateUp() })
            }
            
            composable(Destination.Reports.route) { 
                ReportScreen()
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onNavigateToBackup = { navigator.navigate(Destination.Backup) },
                    onNavigateToSmsDrafts = { navigator.navigate(Destination.SmsDrafts) },
                    onNavigateToAiAssistant = { navigator.navigate(Destination.AiAssistant) },
                    onNavigateToPremium = { navigator.navigate(Destination.Premium) },
                    onNavigateToAbout = { }
                )
            }
            composable(Destination.Backup.route) {
                BackupScreen(onNavigateBack = { navigator.navigateUp() })
            }
            composable(Destination.AiAssistant.route) {
                AiAssistantScreen(onNavigateBack = { navigator.navigateUp() })
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
                Text("Premium Screen")
            }
        }
    }
}
