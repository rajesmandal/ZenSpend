package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionDetailsUiState
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionDetailsViewModel

@Composable
fun TransactionDetailsScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.load(transactionId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(transactionId, onNavigateBack)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Transaction",
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(transactionId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = { viewModel.load(transactionId) },
                modifier = Modifier.padding(padding)
            )
            else -> TransactionDetailsContent(
                state = state,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun TransactionDetailsContent(
    state: TransactionDetailsUiState,
    modifier: Modifier = Modifier
) {
    val amountType = when (state.type) {
        TransactionType.INCOME -> AmountType.INCOME
        TransactionType.EXPENSE -> AmountType.EXPENSE
        TransactionType.TRANSFER -> AmountType.TRANSFER
        else -> AmountType.NEUTRAL
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
    ) {
        ZenSurfaceCard {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            AmountText(
                amount = "₹${state.amount}",
                type = amountType,
                style = AmountHero
            )
        }

        ZenSurfaceCard {
            DetailRow(label = "Category", value = state.categoryName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(label = "Wallet", value = state.walletName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(label = "Date", value = state.date)
            state.note?.takeIf { it.isNotBlank() }?.let { note ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(label = "Note", value = note)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionDetailsPreview() {
    ZenSpendTheme {
        TransactionDetailsContent(
            state = TransactionDetailsUiState(
                isLoading = false,
                title = "Groceries",
                amount = "1,250.00",
                type = TransactionType.EXPENSE,
                date = "31 Aug 2026, 14:30",
                categoryName = "Food",
                walletName = "Cash"
            )
        )
    }
}
