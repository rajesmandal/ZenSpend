package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEffect
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEvent
import com.android.zenspend.feature.wallet.presentation.viewmodel.WalletViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is WalletUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is WalletUiEffect.NavigateToDetails -> onNavigateToDetails(effect.id)
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallets", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Wallet")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.wallets.isEmpty()) {
            EmptyState(
                title = "No Wallets",
                message = "Add a wallet to start tracking your finances",
                icon = Icons.Default.Wallet,
                action = {
                    ZenPrimaryButton(text = "Add Wallet", onClick = onNavigateToAdd)
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                item {
                    BalanceSummary(
                        total = state.totalBalance,
                        assets = state.assetBalance,
                        liabilities = state.liabilityBalance
                    )
                }

                items(state.wallets) { wallet ->
                    WalletItem(
                        name = wallet.name,
                        type = wallet.type.name,
                        balance = wallet.balance,
                        isLiability = wallet.isLiability,
                        onClick = { onNavigateToDetails(wallet.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceSummary(
    total: String,
    assets: String,
    liabilities: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        SummaryCard(
            title = "Net Worth",
            amount = "₹$total",
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            ZenCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
                    Text("Assets", style = MaterialTheme.typography.labelSmall)
                    Text("₹$assets", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                }
            }
            ZenCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
                    Text("Liabilities", style = MaterialTheme.typography.labelSmall)
                    Text("₹$liabilities", style = MaterialTheme.typography.titleMedium, color = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun WalletItem(
    name: String,
    type: String,
    balance: String,
    isLiability: Boolean,
    onClick: () -> Unit
) {
    ZenCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.SpacingLarge)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "₹$balance",
                style = MaterialTheme.typography.titleLarge,
                color = if (isLiability) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
