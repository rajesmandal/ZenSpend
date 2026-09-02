package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEffect
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEvent
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is TransactionUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is TransactionUiEffect.NavigateToDetails -> onNavigateToDetails(effect.id)
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Transactions",
                actions = {
                    IconButton(onClick = { /* Open Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Open Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.transactions.isEmpty()) {
            EmptyState(
                title = "No Transactions",
                message = "Start tracking your expenses and income here.",
                modifier = Modifier.padding(padding),
                action = {
                    ZenPrimaryButton(text = "Add Transaction", onClick = onNavigateToAdd)
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Simplified grouping logic for scaffolding
                items(state.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToDetails(transaction.id) }
                    )
                }

                item {
                    com.android.zenspend.core.monetization.presentation.components.BannerAd(
                        adController = viewModel.adController,
                        unitId = com.android.zenspend.core.monetization.AdConfig.TRANSACTION_LIST_BANNER_ID,
                        currentRoute = "transactions",
                        modifier = Modifier.padding(Dimensions.SpacingLarge)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionUiModel,
    onClick: () -> Unit
) {
    val color = when (transaction.type) {
        TransactionType.INCOME -> ZenSpendTheme.colors.income
        TransactionType.EXPENSE -> ZenSpendTheme.colors.expense
        TransactionType.TRANSFER -> ZenSpendTheme.colors.transfer
        TransactionType.CREDIT_CARD_PAYMENT -> ZenSpendTheme.colors.creditCard
        TransactionType.REFUND -> ZenSpendTheme.colors.income
    }

    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.REFUND -> "+"
        else -> ""
    }

    ZenCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingXSmall)
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.SpacingLarge)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${transaction.categoryName} • ${transaction.walletName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$prefix₹${transaction.amount}",
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
