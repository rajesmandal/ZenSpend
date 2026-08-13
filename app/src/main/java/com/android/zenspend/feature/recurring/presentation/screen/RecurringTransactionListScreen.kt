package com.android.zenspend.feature.recurring.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenCard
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ExpenseRed
import com.android.zenspend.core.ui.theme.IncomeGreen
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import com.android.zenspend.feature.recurring.presentation.viewmodel.RecurringViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Rules") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                val active = state.recurringTransactions.filter { it.isActive }
                val paused = state.recurringTransactions.filter { !it.isActive }

                if (active.isNotEmpty()) {
                    item { Text("Active", style = MaterialTheme.typography.titleMedium) }
                    items(active) { rule ->
                        RecurringRuleCard(
                            rule = rule,
                            onConfirm = { viewModel.handleEvent(RecurringUiEvent.ConfirmOccurrence(rule.id, rule.nextExecutionDate)) },
                            onClick = { onNavigateToDetails(rule.id) }
                        )
                    }
                }

                if (paused.isNotEmpty()) {
                    item { Text("Paused", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = Dimensions.SpacingMedium)) }
                    items(paused) { rule ->
                        RecurringRuleCard(
                            rule = rule,
                            onConfirm = { /* No confirm for paused */ },
                            onClick = { onNavigateToDetails(rule.id) }
                        )
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
    onClick: () -> Unit
) {
    ZenCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${rule.recurrenceType} • Next: ${formatDate(rule.nextExecutionDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "₹${formatAmount(rule.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.type == com.android.zenspend.core.database.model.TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            }
            
            if (rule.isActive && rule.isOverdue) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = Dimensions.SpacingMedium, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text("Confirm Now", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun formatAmount(amount: Long): String {
    return "%.2f".format(amount / 100.0)
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}
