package com.android.zenspend.feature.people.presentation.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSecondaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenTransactionRow
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.people.domain.model.LoanSchedule
import com.android.zenspend.feature.people.presentation.contract.LoanDetailsUiEffect
import com.android.zenspend.feature.people.presentation.contract.LoanDetailsUiEvent
import com.android.zenspend.feature.people.presentation.viewmodel.LoanDetailsViewModel
import kotlinx.coroutines.flow.collectLatest

private enum class LoanDialog(val titleRes: Int, val bodyRes: Int?) {
    UNDO(R.string.loan_undo_last, null),
    CLOSE(R.string.loan_close_title, R.string.loan_close_body),
    DELETE(R.string.loan_delete_title, R.string.loan_delete_body)
}

@Composable
fun LoanDetailsScreen(
    onNavigateBack: () -> Unit,
    onEdit: (loanId: Long) -> Unit,
    viewModel: LoanDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<LoanDialog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is LoanDetailsUiEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.messageRes))
                LoanDetailsUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    dialog?.let { d ->
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(d.titleRes)) },
            text = d.bodyRes?.let { body -> { Text(stringResource(body)) } },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    viewModel.handleEvent(
                        when (d) {
                            LoanDialog.UNDO -> LoanDetailsUiEvent.UndoLast
                            LoanDialog.CLOSE -> LoanDetailsUiEvent.Close
                            LoanDialog.DELETE -> LoanDetailsUiEvent.Delete
                        }
                    )
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }

    val loan = state.loan
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = loan?.title?.ifBlank { null } ?: stringResource(R.string.loan_details_title),
                subtitle = state.personName.ifBlank { null },
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (loan != null) {
                        IconButton(onClick = { onEdit(loan.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.loan_form_title_edit))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (loan == null) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            val active = LoanSchedule.isActive(loan)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item {
                    ZenTile(
                        modifier = Modifier.fillMaxWidth(),
                        tone = ZenTone.Ink,
                        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
                    ) {
                        ZenTileCaption(stringResource(R.string.loan_remaining))
                        ZenTileValue(
                            text = stringResource(R.string.amount_rupees, formatMinorUnits(LoanSchedule.remainingAmount(loan))),
                            style = AmountHero
                        )
                        ZenProgressBar(
                            progress = LoanSchedule.progress(loan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimensions.SpacingMedium)
                        )
                        Spacer(Modifier.height(Dimensions.SpacingSmall))
                        ZenTileCaption(stringResource(R.string.loan_progress, loan.receivedInstallments, loan.totalTenure))
                        Spacer(Modifier.height(Dimensions.SpacingLarge))
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXLarge)) {
                            Column {
                                ZenTileCaption(stringResource(R.string.loan_given))
                                ZenTileValue(
                                    text = stringResource(R.string.amount_rupees, formatMinorUnits(loan.principalAmount)),
                                    style = AmountMedium
                                )
                            }
                            Column {
                                ZenTileCaption(stringResource(R.string.loan_received))
                                ZenTileValue(
                                    text = stringResource(R.string.amount_rupees, formatMinorUnits(LoanSchedule.receivedAmount(loan))),
                                    style = AmountMedium,
                                    color = ZenSpendTheme.colors.tone(ZenTone.Lime).container
                                )
                            }
                        }
                        LoanSchedule.nextDueDate(loan)?.let { due ->
                            Spacer(Modifier.height(Dimensions.SpacingLarge))
                            ZenTileCaption(
                                stringResource(
                                    if (LoanSchedule.isOverdue(loan)) R.string.emi_item_overdue else R.string.emi_item_next_due,
                                    formatLoanDate(due)
                                )
                            )
                        }
                        val extra = LoanSchedule.extraOverPrincipal(loan)
                        if (extra > 0) {
                            Spacer(Modifier.height(Dimensions.SpacingSmall))
                            ZenTileCaption(stringResource(R.string.loan_extra_over_principal, formatMinorUnits(extra)))
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
                        if (active) {
                            ZenPrimaryButton(
                                text = stringResource(R.string.loan_mark_received),
                                onClick = { viewModel.handleEvent(LoanDetailsUiEvent.MarkReceived) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (state.receipts.isNotEmpty()) {
                            ZenSecondaryButton(
                                text = stringResource(R.string.loan_undo_last),
                                onClick = { dialog = LoanDialog.UNDO }
                            )
                        }
                        if (active) {
                            ZenSecondaryButton(
                                text = stringResource(R.string.loan_close),
                                onClick = { dialog = LoanDialog.CLOSE }
                            )
                        }
                        TextButton(onClick = { dialog = LoanDialog.DELETE }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.loan_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                item { ZenSectionHeader(title = stringResource(R.string.loan_history)) }
                if (state.receipts.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.loan_history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(state.receipts.reversed(), key = { it.id }) { receipt ->
                        ZenTransactionRow(
                            title = stringResource(R.string.loan_history_row, receipt.installmentNumber, formatLoanDate(receipt.receivedDate)),
                            subtitle = stringResource(R.string.emi_item_next_due, formatLoanDate(receipt.dueDate)),
                            amount = stringResource(R.string.amount_rupees, formatMinorUnits(receipt.amount)),
                            badgeTone = ZenTone.Lime
                        )
                    }
                }
            }
        }
    }
}
