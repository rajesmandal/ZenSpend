package com.android.zenspend.feature.emi.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSegmentedControl
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EmiPortfolio
import com.android.zenspend.feature.emi.domain.model.EmiSchedule
import com.android.zenspend.feature.emi.presentation.contract.EMIUiEvent
import com.android.zenspend.feature.emi.presentation.contract.EmiFilter
import com.android.zenspend.feature.emi.presentation.viewmodel.EMIViewModel

@Composable
fun EMIListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: EMIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filters = EmiFilter.entries
    val filterLabels = filters.map { stringResource(it.labelRes()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.emi_title),
                showBack = onNavigateBack != null,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cards_add_emi))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.allEmis.isEmpty() -> EmptyState(
                title = stringResource(R.string.emi_empty_title),
                message = stringResource(R.string.emi_empty_body),
                icon = Icons.Default.ReceiptLong,
                action = { ZenPrimaryButton(text = stringResource(R.string.cards_add_emi), onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.ScreenPadding,
                    vertical = Dimensions.SpacingLarge
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item { PortfolioTile(portfolio = state.portfolio) }
                item {
                    ZenSegmentedControl(
                        options = listOf(
                            stringResource(R.string.emi_tab_active),
                            stringResource(R.string.emi_tab_closed)
                        ),
                        selectedIndex = if (state.showClosed) 1 else 0,
                        onSelected = { viewModel.handleEvent(EMIUiEvent.ShowClosed(it == 1)) }
                    )
                }
                item {
                    ZenChipRow(
                        options = filterLabels,
                        selectedIndex = filters.indexOf(state.filter),
                        onSelected = { viewModel.handleEvent(EMIUiEvent.SelectFilter(filters[it])) },
                        contentPadding = PaddingValues()
                    )
                }

                val visible = state.visibleEmis
                if (visible.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(
                                if (state.showClosed) R.string.emi_empty_closed_title else R.string.emi_empty_filter_title
                            ),
                            message = stringResource(
                                if (state.showClosed) R.string.emi_empty_closed_body else R.string.emi_empty_filter_body
                            ),
                            icon = Icons.Default.ReceiptLong
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { emi ->
                        EMIItem(emi = emi, onClick = { onNavigateToDetails(emi.id) })
                    }
                }
            }
        }
    }
}

private fun EmiFilter.labelRes(): Int = when (this) {
    EmiFilter.ALL -> R.string.emi_filter_all
    EmiFilter.CARD -> R.string.emi_filter_card
    EmiFilter.BANK -> R.string.emi_filter_bank
    EmiFilter.PERSON -> R.string.emi_filter_person
    EmiFilter.OTHER -> R.string.emi_filter_other
}

/** Top tile: what you pay every month, how much is left and how much is already gone. */
@Composable
private fun PortfolioTile(portfolio: EmiPortfolio) {
    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        ZenTileCaption(stringResource(R.string.emi_summary_monthly))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, formatMinorUnits(portfolio.monthlyTotal)),
            style = AmountHero
        )
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXLarge)) {
            Column {
                ZenTileCaption(stringResource(R.string.emi_summary_remaining))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(portfolio.remainingTotal)),
                    style = AmountMedium,
                    color = ZenSpendTheme.colors.tone(ZenTone.Coral).container
                )
            }
            Column {
                ZenTileCaption(stringResource(R.string.emi_summary_paid))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(portfolio.paidTotal)),
                    style = AmountMedium,
                    color = ZenSpendTheme.colors.tone(ZenTone.Lime).container
                )
            }
        }
        portfolio.nextDue?.let { next ->
            Spacer(Modifier.height(Dimensions.SpacingLarge))
            ZenTileCaption(
                stringResource(
                    R.string.emi_summary_next,
                    next.name,
                    formatMinorUnits(next.emiAmount),
                    EmiSchedule.nextDueDate(next)?.let(::formatEmiDate).orEmpty()
                )
            )
        }
    }
}

@Composable
fun EMIItem(emi: EMI, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val paid = EmiSchedule.paidInstallments(emi)
    val nextDue = EmiSchedule.nextDueDate(emi)
    val overdue = EmiSchedule.isOverdue(emi)
    val finished = emi.status != EMIStatus.ACTIVE || emi.remainingInstallments <= 0

    ZenTile(modifier = modifier.fillMaxWidth(), tone = emi.lenderType.tone(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            ZenBadge(
                label = emi.lenderName.ifBlank { emi.name },
                icon = emi.lenderType.icon(),
                tone = ZenTone.Surface
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(emi.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                ZenTileCaption(
                    emi.lenderName.ifBlank { stringResource(emi.lenderType.labelRes()) }
                )
            }
            ZenTileValue(text = stringResource(R.string.cards_emi_per_month, formatMinorUnits(emi.emiAmount)))
        }
        ZenProgressBar(
            progress = EmiSchedule.progress(emi).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingMedium)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingSmall),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ZenTileCaption(stringResource(R.string.emi_item_progress, paid, emi.totalTenure))
            ZenTileCaption(
                when {
                    finished && emi.status == EMIStatus.CANCELLED -> stringResource(R.string.emi_item_closed)
                    finished -> stringResource(R.string.emi_item_completed)
                    else -> stringResource(R.string.emi_item_remaining, formatMinorUnits(EmiSchedule.remainingAmount(emi)))
                }
            )
        }
        if (nextDue != null) {
            Text(
                text = stringResource(
                    if (overdue) R.string.emi_item_overdue else R.string.emi_item_next_due,
                    formatEmiDate(nextDue)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (overdue) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
            )
        }
    }
}
