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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSecondaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
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
import com.android.zenspend.feature.emi.domain.model.EmiSchedule
import com.android.zenspend.feature.emi.presentation.contract.EMIDetailsUiEffect
import com.android.zenspend.feature.emi.presentation.contract.EMIDetailsUiEvent
import com.android.zenspend.feature.emi.presentation.contract.PayWalletOption
import com.android.zenspend.feature.emi.presentation.viewmodel.EMIDetailsViewModel
import kotlinx.coroutines.flow.collectLatest

private enum class EmiDialog { NONE, PAY, UNDO, CLOSE, DELETE }

private const val PERCENT = 100

@Composable
fun EMIDetailsScreen(
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: EMIDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var dialog by remember { mutableStateOf(EmiDialog.NONE) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is EMIDetailsUiEffect.ShowMessage -> snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                EMIDetailsUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    val emi = state.emi
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = emi?.name ?: stringResource(R.string.emi_title),
                subtitle = stringResource(R.string.emi_details_subtitle),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (emi != null) {
                        IconButton(onClick = { onEdit(emi.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            emi == null -> EmptyState(
                title = stringResource(R.string.emi_not_found),
                message = "",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item { SummaryTile(emi) }
                item { FactsTile(emi) }
                item { ActionButtons(emi, state.payments.isNotEmpty()) { dialog = it } }
                item { ZenSectionHeader(title = stringResource(R.string.emi_schedule_title)) }
                items(scheduleRows(emi, state.payments.associateBy { it.installmentNumber }), key = { it.number }) { row ->
                    ScheduleRow(row)
                }
            }
        }
    }

    val current = emi
    if (current != null) {
        when (dialog) {
            EmiDialog.NONE -> Unit
            EmiDialog.PAY -> PayDialog(
                installmentNumber = EmiSchedule.paidInstallments(current) + 1,
                wallets = state.wallets,
                onPick = { walletId ->
                    dialog = EmiDialog.NONE
                    viewModel.handleEvent(EMIDetailsUiEvent.MarkPaid(walletId))
                },
                onDismiss = { dialog = EmiDialog.NONE }
            )
            EmiDialog.UNDO -> ConfirmDialog(R.string.emi_undo_title, R.string.emi_undo_body, onDismiss = { dialog = EmiDialog.NONE }) {
                viewModel.handleEvent(EMIDetailsUiEvent.UndoLast)
            }
            EmiDialog.CLOSE -> ConfirmDialog(R.string.emi_close_title, R.string.emi_close_body, onDismiss = { dialog = EmiDialog.NONE }) {
                viewModel.handleEvent(EMIDetailsUiEvent.Close)
            }
            EmiDialog.DELETE -> ConfirmDialog(R.string.emi_delete_title, R.string.emi_delete_body, onDismiss = { dialog = EmiDialog.NONE }) {
                viewModel.handleEvent(EMIDetailsUiEvent.Delete)
            }
        }
    }
}

@Composable
private fun SummaryTile(emi: EMI) {
    val paid = EmiSchedule.paidInstallments(emi)
    val percent = (EmiSchedule.progress(emi) * PERCENT).toInt()
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Ink, contentPadding = PaddingValues(Dimensions.SpacingXLarge)) {
        ZenTileCaption(stringResource(R.string.emi_detail_remaining))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, formatMinorUnits(EmiSchedule.remainingAmount(emi))),
            style = AmountHero
        )
        ZenProgressBar(
            progress = EmiSchedule.progress(emi).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingLarge)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingSmall),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ZenTileCaption(stringResource(R.string.emi_item_progress, paid, emi.totalTenure))
            ZenTileCaption(stringResource(R.string.emi_detail_percent_done, percent))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXLarge)
        ) {
            Column {
                ZenTileCaption(stringResource(R.string.emi_detail_paid))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(EmiSchedule.paidAmount(emi))),
                    style = AmountMedium,
                    color = ZenSpendTheme.colors.tone(ZenTone.Lime).container
                )
            }
            Column {
                ZenTileCaption(stringResource(R.string.emi_detail_per_month))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(emi.emiAmount)),
                    style = AmountMedium
                )
            }
        }
    }
}

@Composable
private fun FactsTile(emi: EMI) {
    val nextDue = EmiSchedule.nextDueDate(emi)
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = emi.lenderType.tone()) {
        Fact(
            stringResource(R.string.emi_detail_owed_to),
            emi.lenderName.ifBlank { stringResource(emi.lenderType.labelRes()) } + " • " + stringResource(emi.lenderType.labelRes())
        )
        Fact(stringResource(R.string.emi_detail_principal), stringResource(R.string.amount_rupees, formatMinorUnits(emi.principalAmount)))
        Fact(stringResource(R.string.emi_detail_total_payable), stringResource(R.string.amount_rupees, formatMinorUnits(EmiSchedule.totalPayable(emi))))
        Fact(stringResource(R.string.emi_detail_total_interest), stringResource(R.string.amount_rupees, formatMinorUnits(EmiSchedule.totalInterest(emi))))
        emi.interestRate?.let { Fact(stringResource(R.string.emi_detail_interest_rate), "$it%") }
        Fact(stringResource(R.string.emi_detail_due_day, emi.dueDay), stringResource(R.string.emi_detail_installments_left, emi.remainingInstallments))
        if (nextDue != null) Fact(stringResource(R.string.emi_detail_next_due), formatEmiDate(nextDue))
        Fact(stringResource(R.string.emi_detail_last_due), formatEmiDate(EmiSchedule.finalDueDate(emi)))
        emi.note?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(Dimensions.SpacingSmall))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingXSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ZenTileCaption(label)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ActionButtons(emi: EMI, hasPayments: Boolean, open: (EmiDialog) -> Unit) {
    val active = emi.status == EMIStatus.ACTIVE && emi.remainingInstallments > 0
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
        if (active) {
            ZenPrimaryButton(
                text = stringResource(R.string.emi_action_mark_paid),
                onClick = { open(EmiDialog.PAY) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (hasPayments) {
            ZenSecondaryButton(
                text = stringResource(R.string.emi_action_undo),
                onClick = { open(EmiDialog.UNDO) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (emi.status == EMIStatus.ACTIVE) {
            ZenSecondaryButton(
                text = stringResource(R.string.emi_action_close),
                onClick = { open(EmiDialog.CLOSE) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        TextButton(onClick = { open(EmiDialog.DELETE) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.emi_action_delete), color = MaterialTheme.colorScheme.error)
        }
    }
}

private data class ScheduleItem(val number: Int, val dueDate: Long, val paidDate: Long?)

private fun scheduleRows(emi: EMI, payments: Map<Int, com.android.zenspend.feature.emi.domain.model.EMIPayment>): List<ScheduleItem> {
    val paidCount = EmiSchedule.paidInstallments(emi)
    return (1..emi.totalTenure).map { n ->
        val payment = payments[n]
        ScheduleItem(
            number = n,
            dueDate = EmiSchedule.installmentDueDate(emi, n),
            // Installments paid before the EMI was added have no payment record; still count as paid.
            paidDate = payment?.paidDate ?: if (n <= paidCount) 0L else null
        )
    }
}

@Composable
private fun ScheduleRow(item: ScheduleItem) {
    val paid = item.paidDate != null
    val overdue = !paid && item.dueDate < System.currentTimeMillis()
    val tone = if (paid) ZenTone.Mint else ZenTone.Surface
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = tone) {
        Text(stringResource(R.string.emi_schedule_number, item.number), style = MaterialTheme.typography.titleSmall)
        val caption = when {
            paid && item.paidDate != 0L -> stringResource(R.string.emi_schedule_paid, formatEmiDate(item.paidDate!!))
            paid -> stringResource(R.string.emi_schedule_paid_earlier)
            overdue -> stringResource(R.string.emi_schedule_overdue, formatEmiDate(item.dueDate))
            else -> stringResource(R.string.emi_schedule_due, formatEmiDate(item.dueDate))
        }
        Text(
            caption,
            style = MaterialTheme.typography.bodySmall,
            color = if (overdue) ZenSpendTheme.colors.expense else ZenSpendTheme.colors.tone(tone).contentMuted
        )
    }
}

@Composable
private fun PayDialog(
    installmentNumber: Int,
    wallets: List<PayWalletOption>,
    onPick: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.emi_pay_title, installmentNumber)) },
        text = {
            Column {
                Text(stringResource(R.string.emi_pay_body))
                Spacer(Modifier.height(Dimensions.SpacingSmall))
                wallets.forEach { wallet ->
                    TextButton(onClick = { onPick(wallet.id) }, modifier = Modifier.fillMaxWidth()) { Text(wallet.name) }
                }
                TextButton(onClick = { onPick(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.emi_pay_none))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun ConfirmDialog(titleRes: Int, bodyRes: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(bodyRes)) },
        confirmButton = {
            TextButton(onClick = { onDismiss(); onConfirm() }) { Text(stringResource(R.string.action_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
