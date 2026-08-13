package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.wallet.presentation.contract.WalletDetailsState
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEffect
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEvent
import com.android.zenspend.feature.wallet.presentation.viewmodel.WalletViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailsScreen(
    walletId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(walletId) {
        viewModel.handleEvent(WalletUiEvent.LoadWalletDetails(walletId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is WalletUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is WalletUiEffect.NavigateBack -> onNavigateBack()
                is WalletUiEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(walletId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { viewModel.handleEvent(WalletUiEvent.ArchiveWallet(walletId)) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val detailState = state.detailsState) {
            is WalletDetailsState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is WalletDetailsState.Error -> ErrorState(message = detailState.message, onRetry = { viewModel.handleEvent(WalletUiEvent.LoadWalletDetails(walletId)) })
            is WalletDetailsState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimensions.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                ) {
                    item {
                        DetailHeader(
                            name = detailState.wallet.name,
                            type = detailState.wallet.type.name,
                            balance = detailState.wallet.balance,
                            isLiability = detailState.wallet.isLiability
                        )
                    }

                    item {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = Dimensions.SpacingMedium)
                        )
                    }

                    if (detailState.recentTransactions.isEmpty()) {
                        item {
                            Text(
                                "No transactions found for this wallet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(detailState.recentTransactions) { tx ->
                            TransactionListItem(
                                title = tx.title,
                                amount = tx.amount,
                                date = tx.date,
                                type = tx.type.name
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun DetailHeader(
    name: String,
    type: String,
    balance: String,
    isLiability: Boolean
) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimensions.SpacingXXLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(type, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
            Text(
                text = "₹$balance",
                style = MaterialTheme.typography.displayLarge,
                color = if (isLiability) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TransactionListItem(
    title: String,
    amount: String,
    date: String,
    type: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "₹$amount",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
