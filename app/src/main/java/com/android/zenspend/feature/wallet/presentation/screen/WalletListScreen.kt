package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.AmountType
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenTransactionRow
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEffect
import com.android.zenspend.feature.wallet.presentation.viewmodel.WalletViewModel
import kotlinx.coroutines.flow.collectLatest

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.wallets_title),
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.wallets_add))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.wallets.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.wallets_empty_title),
                message = stringResource(R.string.wallets_empty_body),
                icon = Icons.Default.Wallet,
                action = {
                    ZenPrimaryButton(
                        text = stringResource(R.string.wallets_add),
                        onClick = onNavigateToAdd
                    )
                },
                modifier = Modifier.padding(padding)
            )
        } else {
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
                    BalanceSummary(
                        total = state.totalBalance,
                        assets = state.assetBalance,
                        liabilities = state.liabilityBalance
                    )
                }

                item {
                    ZenSectionHeader(
                        title = stringResource(R.string.wallets_all_accounts),
                        modifier = Modifier.padding(top = Dimensions.SpacingMedium)
                    )
                }

                items(state.wallets) { wallet ->
                    WalletItem(
                        name = wallet.name,
                        type = wallet.type.name,
                        balance = wallet.balance,
                        isLiability = wallet.isLiability,
                        onClick = { onNavigateToDetails(wallet.id) },
                        tone = ZenTone.forKey(wallet.id)
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
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
    ) {
        ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Ink) {
            ZenTileCaption(stringResource(R.string.wallets_net_worth))
            Spacer(Modifier.height(Dimensions.SpacingXSmall))
            ZenTileValue(
                text = stringResource(R.string.amount_rupees, total),
                style = AmountHero
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
        ) {
            ZenTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                tone = ZenTone.Lime
            ) {
                ZenTileCaption(stringResource(R.string.wallets_assets))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, assets),
                    style = AmountMedium
                )
            }
            ZenTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                tone = ZenTone.Coral
            ) {
                ZenTileCaption(stringResource(R.string.wallets_liabilities))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, liabilities),
                    style = AmountMedium
                )
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
    onClick: () -> Unit,
    tone: ZenTone = ZenTone.forKey(name)
) {
    ZenTransactionRow(
        title = name,
        subtitle = type,
        amount = stringResource(R.string.amount_rupees, balance),
        amountType = if (isLiability) AmountType.EXPENSE else AmountType.NEUTRAL,
        badgeTone = tone,
        onClick = onClick
    )
}
