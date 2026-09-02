package com.android.zenspend.feature.creditcard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
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
            title = { Text("Mark installment paid?") },
            text = { Text("This updates EMI progress only. No transaction will be created.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.handleEvent(CreditCardDetailsUiEvent.MarkEmiPaid(emiId))
                    emiToMarkPaid = null
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { emiToMarkPaid = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = state.cardName.ifBlank { "Credit Card" },
                subtitle = if (state.bankName.isNotBlank()) {
                    "${state.bankName} •••• ${state.lastFourDigits}"
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
                text = { Text("Add EMI") },
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
                contentPadding = PaddingValues(Dimensions.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
            ) {
                item {
                    ZenSurfaceCard {
                        Text(
                            "Outstanding Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                        AmountText(
                            amount = "₹${state.outstanding}",
                            type = AmountType.EXPENSE,
                            style = AmountHero
                        )
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Available", style = MaterialTheme.typography.labelSmall)
                                AmountText(amount = "₹${state.availableLimit}", type = AmountType.INCOME)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Limit", style = MaterialTheme.typography.labelSmall)
                                AmountText(amount = "₹${state.creditLimit}", type = AmountType.NEUTRAL)
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                        LinearProgressIndicator(
                            progress = { state.utilization.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (state.utilization > 0.8f) {
                                ZenSpendTheme.colors.expense
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Active EMIs (${state.activeEmis.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (state.activeEmis.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No EMIs on this card",
                            message = "Track purchases converted to EMIs with start date, amount, and tenure.",
                            action = {
                                ZenPrimaryButton(
                                    text = "Add EMI",
                                    onClick = { onNavigateToAddEmi(cardId) }
                                )
                            }
                        )
                    }
                } else {
                    items(state.activeEmis, key = { it.id }) { emi ->
                        CardEmiItem(
                            emi = emi,
                            onMarkPaid = { emiToMarkPaid = emi.id }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge)) }
            }
        }
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

    ZenSurfaceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(emi.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${emi.remainingInstallments}/${emi.totalTenure} months left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountText(
                amount = "₹${"%.2f".format(emi.emiAmount / 100.0)}/mo",
                type = AmountType.NEUTRAL
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
        Text(
            text = "${dateFormat.format(Date(emi.startDate))} → ${dateFormat.format(Date(emi.endDate))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Principal ₹${"%.2f".format(emi.principalAmount / 100.0)} • Due day ${emi.dueDay}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        ZenSecondaryButton(
            text = "Mark 1 paid",
            onClick = onMarkPaid,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
