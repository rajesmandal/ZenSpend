package com.android.zenspend.feature.creditcard.presentation.screen

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.ErrorState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSecondaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenStat
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardDetailsUiEffect
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardDetailsUiEvent
import com.android.zenspend.feature.creditcard.presentation.viewmodel.CreditCardDetailsViewModel
import com.android.zenspend.feature.emi.domain.model.EMI
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreditCardDetailsScreen(
    cardId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddEmi: (Long) -> Unit,
    viewModel: CreditCardDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var emiToMarkPaid by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(cardId) {
        viewModel.handleEvent(CreditCardDetailsUiEvent.Load(cardId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CreditCardDetailsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    emiToMarkPaid?.let { emiId ->
        AlertDialog(
            onDismissRequest = { emiToMarkPaid = null },
            title = { Text(stringResource(R.string.cards_mark_paid_title)) },
            text = { Text(stringResource(R.string.cards_mark_paid_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.handleEvent(CreditCardDetailsUiEvent.MarkEmiPaid(emiId))
                    emiToMarkPaid = null
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { emiToMarkPaid = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = state.cardName.ifBlank { stringResource(R.string.cards_default_title) },
                subtitle = if (state.bankName.isNotBlank()) {
                    stringResource(R.string.cards_bank_last_four, state.bankName, state.lastFourDigits)
                } else {
                    null
                },
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddEmi(cardId) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.cards_add_emi)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = { viewModel.handleEvent(CreditCardDetailsUiEvent.Load(cardId)) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = Dimensions.ScreenPadding,
                    end = Dimensions.ScreenPadding,
                    top = Dimensions.SpacingLarge,
                    bottom = Dimensions.FabClearance
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item { OutstandingTile(state.outstanding, state.availableLimit, state.creditLimit, state.utilization) }

                item {
                    ZenSectionHeader(
                        title = stringResource(R.string.cards_active_emis, state.activeEmis.size),
                        modifier = Modifier.padding(top = Dimensions.SpacingSmall)
                    )
                }

                if (state.activeEmis.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.cards_no_emis_title),
                            message = stringResource(R.string.cards_no_emis_body),
                            action = {
                                ZenPrimaryButton(
                                    text = stringResource(R.string.cards_add_emi),
                                    onClick = { onNavigateToAddEmi(cardId) }
                                )
                            }
                        )
                    }
                } else {
                    items(state.activeEmis, key = { it.id }) { emi ->
                        CardEmiItem(emi = emi, onMarkPaid = { emiToMarkPaid = emi.id })
                    }
                }
            }
        }
    }
}

@Composable
private fun OutstandingTile(
    outstanding: String,
    available: String,
    limit: String,
    utilization: Float
) {
    val expenseColor = ZenSpendTheme.colors.expense
    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        ZenTileCaption(stringResource(R.string.cards_outstanding))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, outstanding),
            style = AmountHero,
            modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingXLarge)
        ) {
            ZenStat(
                caption = stringResource(R.string.cards_available),
                value = stringResource(R.string.amount_rupees, available),
                modifier = Modifier.weight(1f)
            )
            ZenStat(
                caption = stringResource(R.string.cards_limit),
                value = stringResource(R.string.amount_rupees, limit),
                modifier = Modifier.weight(1f)
            )
        }
        ZenProgressBar(
            progress = utilization.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingLarge),
            fillColor = if (utilization > UTILIZATION_ALERT_FRACTION) expenseColor else Color.Unspecified
        )
    }
}

@Composable
private fun CardEmiItem(
    emi: EMI,
    onMarkPaid: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val progress = if (emi.totalTenure > 0) {
        (emi.totalTenure - emi.remainingInstallments).toFloat() / emi.totalTenure
    } else {
        0f
    }

    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.forKey(emi.id)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(emi.name, style = MaterialTheme.typography.titleMedium)
                ZenTileCaption(
                    stringResource(R.string.cards_emi_months_left, emi.remainingInstallments, emi.totalTenure)
                )
            }
            ZenTileValue(
                text = stringResource(R.string.cards_emi_per_month, formatMinorUnits(emi.emiAmount)),
                style = MaterialTheme.typography.titleMedium
            )
        }
        ZenProgressBar(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingMedium)
        )
        ZenTileCaption(
            text = stringResource(
                R.string.cards_emi_range,
                dateFormat.format(Date(emi.startDate)),
                dateFormat.format(Date(emi.endDate))
            ),
            modifier = Modifier.padding(top = Dimensions.SpacingSmall)
        )
        ZenTileCaption(
            stringResource(R.string.cards_emi_principal, formatMinorUnits(emi.principalAmount), emi.dueDay)
        )
        ZenSecondaryButton(
            text = stringResource(R.string.cards_mark_paid),
            onClick = onMarkPaid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingMedium)
        )
    }
}
