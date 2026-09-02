package com.android.zenspend.feature.recurring.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import com.android.zenspend.feature.recurring.presentation.viewmodel.RecurringViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionDetailsScreen(
    recurringId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rule = state.recurringTransactions.find { it.id == recurringId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rule Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(recurringId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { 
                        viewModel.handleEvent(RecurringUiEvent.ToggleActive(recurringId, rule?.isActive ?: true))
                    }) {
                        val icon = if (rule?.isActive == true) Icons.Default.Pause else Icons.Default.PlayArrow
                        Icon(icon, contentDescription = "Toggle Status")
                    }
                    IconButton(onClick = { 
                        viewModel.handleEvent(RecurringUiEvent.ToggleActive(recurringId, true)) // Simplified: should be archive
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (rule == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Rule not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                Text(rule.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "₹${"%.2f".format(rule.amount / 100.0)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (rule.type == com.android.zenspend.core.database.model.TransactionType.INCOME) ZenSpendTheme.colors.income else ZenSpendTheme.colors.expense
                )
                
                Divider()
                
                DetailRow(label = "Frequency", value = rule.recurrenceType.name)
                DetailRow(label = "Next Occurrence", value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(rule.nextExecutionDate)))
                DetailRow(label = "Status", value = if (rule.isActive) "Active" else "Paused")
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { viewModel.handleEvent(RecurringUiEvent.ToggleActive(rule.id, rule.isActive)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rule.isActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (rule.isActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(if (rule.isActive) "Pause Rule" else "Resume Rule")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
