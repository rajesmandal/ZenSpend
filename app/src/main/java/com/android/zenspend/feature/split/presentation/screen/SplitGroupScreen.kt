package com.android.zenspend.feature.split.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitGroupDetails
import com.android.zenspend.feature.split.domain.model.SplitMember
import com.android.zenspend.feature.split.domain.model.SplitMath
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import com.android.zenspend.feature.split.domain.model.SplitTransfer
import com.android.zenspend.feature.split.presentation.contract.SplitGroupUiEffect
import com.android.zenspend.feature.split.presentation.contract.SplitGroupUiEvent
import com.android.zenspend.feature.split.presentation.viewmodel.SplitGroupViewModel
import kotlinx.coroutines.flow.collectLatest

private sealed interface PendingDelete {
    data object Group : PendingDelete
    data class Expense(val id: Long) : PendingDelete
    data class Payment(val id: Long) : PendingDelete
}

/** One group: who owes whom, the quickest way to settle, and every expense. */
@Composable
fun SplitGroupScreen(
    onNavigateBack: () -> Unit,
    onAddExpense: (groupId: Long) -> Unit,
    onEditExpense: (expenseId: Long) -> Unit,
    viewModel: SplitGroupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAddMember by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingDelete?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                SplitGroupUiEffect.NavigateBack -> onNavigateBack()
                is SplitGroupUiEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.messageRes))
            }
        }
    }

    val details = state.details
    if (showAddMember) {
        AddMemberDialog(
            onDismiss = { showAddMember = false },
            onAdd = { viewModel.handleEvent(SplitGroupUiEvent.AddMember(it)) }
        )
    }
    pending?.let { p ->
        ConfirmDeleteDialog(
            title = when (p) {
                PendingDelete.Group -> stringResource(R.string.split_delete_group_title, details?.group?.name.orEmpty())
                is PendingDelete.Expense -> stringResource(R.string.split_delete_expense_title)
                is PendingDelete.Payment -> stringResource(R.string.split_delete_payment_title)
            },
            body = when (p) {
                PendingDelete.Group -> stringResource(R.string.split_delete_group_body)
                is PendingDelete.Expense -> stringResource(R.string.split_delete_expense_body)
                is PendingDelete.Payment -> stringResource(R.string.split_delete_payment_body)
            },
            onDismiss = { pending = null },
            onConfirm = {
                viewModel.handleEvent(
                    when (p) {
                        PendingDelete.Group -> SplitGroupUiEvent.DeleteGroup
                        is PendingDelete.Expense -> SplitGroupUiEvent.DeleteExpense(p.id)
                        is PendingDelete.Payment -> SplitGroupUiEvent.DeleteSettlement(p.id)
                    }
                )
                pending = null
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            ZenTopAppBar(
                title = details?.group?.name ?: stringResource(R.string.split_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { pending = PendingDelete.Group }, enabled = details != null) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.split_delete_group))
                    }
                }
            )
        }
    ) { padding ->
        if (details == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
        ) {
            item { SummaryTile(details) }
            item {
                ZenPrimaryButton(
                    text = stringResource(R.string.split_add_expense),
                    onClick = { onAddExpense(details.group.id) },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { SectionTitle(stringResource(R.string.split_settle_header)) }
            if (details.transfers.isEmpty()) {
                item { ZenTileCaption(stringResource(R.string.split_settle_all_even)) }
            } else {
                items(details.transfers, key = { "t${it.from}-${it.to}" }) { t ->
                    TransferRow(
                        details = details,
                        transfer = t,
                        onPaid = { viewModel.handleEvent(SplitGroupUiEvent.SettleUp(t.from, t.to, t.amount)) }
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { SectionTitle(stringResource(R.string.split_members_header)) }
                    IconButton(onClick = { showAddMember = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.split_add_member))
                    }
                }
            }
            items(details.members, key = { "m${it.id}" }) { m ->
                MemberRow(
                    member = m,
                    balance = details.balances[m.id] ?: 0L,
                    onRemove = { viewModel.handleEvent(SplitGroupUiEvent.RemoveMember(m.id)) }
                )
            }
            item { SectionTitle(stringResource(R.string.split_expenses_header)) }
            if (details.expenses.isEmpty()) {
                item { ZenTileCaption(stringResource(R.string.split_no_expenses)) }
            } else {
                items(details.expenses, key = { "e${it.id}" }) { e ->
                    ExpenseRow(
                        expense = e,
                        payer = details.memberName(e.paidBy).orEmpty(),
                        onClick = { onEditExpense(e.id) },
                        onDelete = { pending = PendingDelete.Expense(e.id) }
                    )
                }
            }
            if (details.settlements.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.split_payments_header)) }
                items(details.settlements, key = { "s${it.id}" }) { s ->
                    PaymentRow(details = details, settlement = s, onDelete = { pending = PendingDelete.Payment(s.id) })
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Dimensions.SpacingMedium)
    )
}

@Composable
private fun SummaryTile(details: SplitGroupDetails) {
    val myShare = details.me?.let { SplitMath.shareOf(it.id, details.expenses) } ?: 0L
    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        ZenTileCaption(stringResource(R.string.split_summary_total))
        ZenTileValue(text = rupees(details.totalSpent), style = AmountHero)
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        ZenTileCaption(stringResource(R.string.split_summary_your_share))
        ZenTileValue(text = rupees(myShare), style = AmountMedium)
    }
}

@Composable
private fun TransferRow(details: SplitGroupDetails, transfer: SplitTransfer, onPaid: () -> Unit) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        R.string.split_settle_row,
                        details.memberName(transfer.from).orEmpty(),
                        details.memberName(transfer.to).orEmpty()
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
                ZenTileValue(text = rupees(transfer.amount), style = AmountMedium)
            }
            TextButton(onClick = onPaid) { Text(stringResource(R.string.split_settle_mark_paid)) }
        }
    }
}

@Composable
private fun MemberRow(member: SplitMember, balance: Long, onRemove: () -> Unit) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            ZenBadge(label = member.name, icon = Icons.Default.Person)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(
                    text = when {
                        balance > 0 -> stringResource(R.string.split_balance_gets, rupees(balance))
                        balance < 0 -> stringResource(R.string.split_balance_owes, rupees(-balance))
                        else -> stringResource(R.string.split_balance_even)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        balance > 0 -> ZenSpendTheme.colors.income
                        balance < 0 -> ZenSpendTheme.colors.expense
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (!member.isMe) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.split_remove_member, member.name))
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: SplitExpense, payer: String, onClick: () -> Unit, onDelete: () -> Unit) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                ZenTileCaption(
                    stringResource(R.string.split_expense_meta, payer, rupees(expense.amount), formatSplitDate(expense.date))
                )
                ZenTileCaption(stringResource(R.string.split_expense_with, expense.shares.size))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun PaymentRow(details: SplitGroupDetails, settlement: SplitSettlement, onDelete: () -> Unit) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        R.string.split_payment_row,
                        details.memberName(settlement.from).orEmpty(),
                        details.memberName(settlement.to).orEmpty()
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
                ZenTileCaption("${rupees(settlement.amount)} • ${formatSplitDate(settlement.date)}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun AddMemberDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.split_add_member)) },
        text = { ZenTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.people_name)) },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onAdd(name); onDismiss() }) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun ConfirmDeleteDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
