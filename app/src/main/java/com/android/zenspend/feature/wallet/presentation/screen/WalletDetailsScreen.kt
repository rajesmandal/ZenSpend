package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.ErrorState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.transaction.presentation.screen.TransactionItem
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.wallet_details_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(walletId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = { viewModel.handleEvent(WalletUiEvent.ArchiveWallet(walletId)) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_archive),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val detailState = state.detailsState) {
            is WalletDetailsState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is WalletDetailsState.Error -> ErrorState(
                message = detailState.message,
                onRetry = { viewModel.handleEvent(WalletUiEvent.LoadWalletDetails(walletId)) },
                modifier = Modifier.padding(padding)
            )
            is WalletDetailsState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.ScreenPadding,
                        vertical = Dimensions.SpacingLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
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
                        ZenSectionHeader(
                            title = stringResource(R.string.wallet_details_recent),
                            modifier = Modifier.padding(top = Dimensions.SpacingMedium)
                        )
                    }

                    if (detailState.recentTransactions.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.wallet_details_no_transactions),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(detailState.recentTransactions, key = { it.id }) { tx ->
                            TransactionItem(transaction = tx, onClick = {})
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

/** Ink hero tile: wallet identity + balance. Liabilities show the balance in the expense colour. */
@Composable
fun DetailHeader(
    name: String,
    type: String,
    balance: String,
    isLiability: Boolean,
    modifier: Modifier = Modifier
) {
    ZenTile(
        modifier = modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenBadge(label = name, tone = ZenTone.Lime)
            Column {
                Text(name, style = MaterialTheme.typography.titleLarge)
                ZenTileCaption(type)
            }
        }
        ZenTileCaption(
            text = stringResource(R.string.wallet_details_balance),
            modifier = Modifier.padding(top = Dimensions.SpacingXLarge)
        )
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, balance),
            style = AmountHero,
            color = if (isLiability) ZenSpendTheme.colors.expense else Color.Unspecified
        )
    }
}
