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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.people.domain.model.LoanSchedule
import com.android.zenspend.feature.people.domain.model.PersonLoan
import com.android.zenspend.feature.people.presentation.contract.PersonDetailsUiEffect
import com.android.zenspend.feature.people.presentation.contract.PersonDetailsUiEvent
import com.android.zenspend.feature.people.presentation.viewmodel.PersonDetailsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PersonDetailsScreen(
    onNavigateBack: () -> Unit,
    onGiveLoan: (personId: Long) -> Unit,
    onLoanClick: (loanId: Long) -> Unit,
    viewModel: PersonDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { if (it == PersonDetailsUiEffect.NavigateBack) onNavigateBack() }
    }

    val overview = state.overview
    if (confirmDelete && overview != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.people_delete_title, overview.person.name)) },
            text = { Text(stringResource(R.string.people_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.handleEvent(PersonDetailsUiEvent.Delete)
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = overview?.person?.name.orEmpty(),
                subtitle = overview?.person?.phone,
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { confirmDelete = true }, enabled = overview != null) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.people_delete))
                    }
                }
            )
        }
    ) { padding ->
        if (overview == null) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
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
                        ZenTileCaption(stringResource(R.string.people_summary_to_receive))
                        ZenTileValue(
                            text = stringResource(R.string.amount_rupees, formatMinorUnits(overview.remaining)),
                            style = AmountHero
                        )
                        Spacer(Modifier.height(Dimensions.SpacingSmall))
                        ZenTileCaption(
                            stringResource(R.string.people_summary_received) + ": " +
                                stringResource(R.string.amount_rupees, formatMinorUnits(overview.received))
                        )
                    }
                }
                item {
                    ZenPrimaryButton(
                        text = stringResource(R.string.people_give_loan),
                        icon = Icons.Default.Add,
                        onClick = { onGiveLoan(overview.person.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { ZenSectionHeader(title = stringResource(R.string.people_loans_header)) }
                if (overview.loans.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.people_no_loans),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(overview.loans, key = { it.id }) { loan ->
                        LoanItem(loan = loan, onClick = { onLoanClick(loan.id) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun LoanItem(loan: PersonLoan, onClick: () -> Unit) {
    val active = LoanSchedule.isActive(loan)
    val overdue = LoanSchedule.isOverdue(loan)
    val nextDue = LoanSchedule.nextDueDate(loan)
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = if (active) ZenTone.Lilac else ZenTone.Surface, onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    loan.title.ifBlank { stringResource(R.string.loan_default_title) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                ZenTileCaption(stringResource(R.string.cards_emi_per_month, formatMinorUnits(loan.installmentAmount)))
            }
            ZenTileValue(text = stringResource(R.string.amount_rupees, formatMinorUnits(loan.principalAmount)))
        }
        ZenProgressBar(
            progress = LoanSchedule.progress(loan),
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
            ZenTileCaption(stringResource(R.string.loan_progress, loan.receivedInstallments, loan.totalTenure))
            ZenTileCaption(
                when {
                    active -> stringResource(R.string.emi_item_remaining, formatMinorUnits(LoanSchedule.remainingAmount(loan)))
                    loan.isClosed -> stringResource(R.string.loan_closed_label)
                    else -> stringResource(R.string.loan_completed_label)
                }
            )
        }
        if (nextDue != null) {
            Text(
                text = stringResource(
                    if (overdue) R.string.emi_item_overdue else R.string.emi_item_next_due,
                    formatLoanDate(nextDue)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (overdue) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
            )
        }
    }
}
