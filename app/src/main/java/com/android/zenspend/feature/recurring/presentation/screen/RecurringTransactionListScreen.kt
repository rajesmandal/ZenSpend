package com.android.zenspend.feature.recurring.presentation.screen

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import com.android.zenspend.feature.recurring.presentation.viewmodel.RecurringViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.recurring_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.recurring_add))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.recurringTransactions.isEmpty() -> EmptyState(
                title = stringResource(R.string.recurring_empty_title),
                message = stringResource(R.string.recurring_empty_body),
                action = { ZenPrimaryButton(text = stringResource(R.string.recurring_add), onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
            else -> {
                val active = state.recurringTransactions.filter { it.isActive }
                val paused = state.recurringTransactions.filter { !it.isActive }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.ScreenPadding,
                        vertical = Dimensions.SpacingLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
                ) {
                    if (active.isNotEmpty()) {
                        item { ZenSectionHeader(title = stringResource(R.string.recurring_active)) }
                        items(active, key = { it.id }) { rule ->
                            RecurringRuleCard(
                                rule = rule,
                                onConfirm = {
                                    viewModel.handleEvent(
                                        RecurringUiEvent.ConfirmOccurrence(rule.id, rule.nextExecutionDate)
                                    )
                                },
                                onClick = { onNavigateToDetails(rule.id) }
                            )
                        }
                    }
                    if (paused.isNotEmpty()) {
                        item { ZenSectionHeader(title = stringResource(R.string.recurring_paused)) }
                        items(paused, key = { it.id }) { rule ->
                            RecurringRuleCard(rule = rule, onConfirm = {}, onClick = { onNavigateToDetails(rule.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringRuleCard(
    rule: RecurringTransaction,
    onConfirm: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(rule.nextExecutionDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(rule.nextExecutionDate))
    }
    val isIncome = rule.type == TransactionType.INCOME
    val overdue = rule.isActive && rule.isOverdue
    // Overdue rules stand out with the coral tone; paused rules fall back to the neutral surface.
    val tone = when {
        overdue -> ZenTone.Coral
        rule.isActive -> ZenTone.forKey(rule.name)
        else -> ZenTone.Surface
    }

    ZenTile(modifier = modifier.fillMaxWidth(), tone = tone, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.name, style = MaterialTheme.typography.titleMedium)
                ZenTileCaption(
                    stringResource(R.string.recurring_type_next, rule.recurrenceType.name, dateText)
                )
            }
            val template = if (isIncome) R.string.amount_rupees_income else R.string.amount_rupees_expense
            ZenTileValue(
                text = stringResource(template, formatMinorUnits(rule.amount)),
                style = AmountMedium,
                color = if (tone == ZenTone.Surface) {
                    if (isIncome) ZenSpendTheme.colors.income else ZenSpendTheme.colors.expense
                } else {
                    androidx.compose.ui.graphics.Color.Unspecified
                }
            )
        }
        if (overdue) {
            ZenPrimaryButton(
                text = stringResource(R.string.recurring_confirm_now),
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.SpacingMedium)
            )
        }
    }
}
