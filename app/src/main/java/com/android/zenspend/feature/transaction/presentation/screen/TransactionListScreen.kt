package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.AmountType
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenTransactionRow
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEffect
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

/** Filter chips shown above the list. Order == chip order. */
private enum class TransactionFilter(val labelRes: Int, val matches: (TransactionType) -> Boolean) {
    All(R.string.filter_all, { true }),
    Expense(R.string.filter_expense, { it == TransactionType.EXPENSE }),
    Income(R.string.filter_income, { it == TransactionType.INCOME || it == TransactionType.REFUND }),
    Transfer(
        R.string.filter_transfer,
        { it == TransactionType.TRANSFER || it == TransactionType.CREDIT_CARD_PAYMENT }
    )
}

@Composable
fun TransactionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var filterIndex by rememberSaveable { mutableIntStateOf(0) }
    val filter = TransactionFilter.entries[filterIndex]

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.transactions_title),
                actions = {
                    IconButton(onClick = { /* Open Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = { /* Open Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.action_filter))
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
                title = stringResource(R.string.transactions_empty_title),
                message = stringResource(R.string.transactions_empty_body),
                modifier = Modifier.padding(padding),
                action = {
                    ZenPrimaryButton(
                        text = stringResource(R.string.transactions_add),
                        onClick = onNavigateToAdd
                    )
                }
            )
        } else {
            val grouped = state.transactions
                .filter { filter.matches(it.type) }
                .groupBy { it.date }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = Dimensions.SpacingXLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                item {
                    ZenChipRow(
                        options = TransactionFilter.entries.map { stringResource(it.labelRes) },
                        selectedIndex = filterIndex,
                        onSelected = { filterIndex = it },
                        modifier = Modifier.padding(vertical = Dimensions.SpacingSmall)
                    )
                }

                grouped.forEach { (dateLabel, rows) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(
                                start = Dimensions.ScreenPadding,
                                end = Dimensions.ScreenPadding,
                                top = Dimensions.SpacingMedium
                            )
                        )
                    }
                    items(rows, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onNavigateToDetails(transaction.id) },
                            modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)
                        )
                    }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (amountType, template) = when (transaction.type) {
        TransactionType.INCOME, TransactionType.REFUND ->
            AmountType.INCOME to R.string.amount_rupees_income
        TransactionType.EXPENSE ->
            AmountType.EXPENSE to R.string.amount_rupees_expense
        TransactionType.TRANSFER ->
            AmountType.TRANSFER to R.string.amount_rupees
        TransactionType.CREDIT_CARD_PAYMENT ->
            AmountType.NEUTRAL to R.string.amount_rupees
    }

    ZenTransactionRow(
        title = transaction.title.ifBlank { stringResource(R.string.untitled) },
        subtitle = "${transaction.categoryName} • ${transaction.walletName}",
        amount = stringResource(template, transaction.amount),
        amountType = amountType,
        badgeTone = ZenTone.forKey(transaction.categoryName),
        onClick = onClick,
        modifier = modifier
    )
}
