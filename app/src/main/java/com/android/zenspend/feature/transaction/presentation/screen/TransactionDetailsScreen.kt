package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.R
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
            title = { Text(stringResource(R.string.txn_delete_title)) },
            text = { Text(stringResource(R.string.txn_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(transactionId, onNavigateBack)
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.txn_details_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(transactionId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete),
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
    // Hero tone reflects the transaction type: income = Lime, expense = Coral, everything else = Ink.
    val heroTone = when (state.type) {
        TransactionType.INCOME, TransactionType.REFUND -> ZenTone.Lime
        TransactionType.EXPENSE -> ZenTone.Coral
        else -> ZenTone.Ink
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
    ) {
        ZenTile(
            modifier = Modifier.fillMaxWidth(),
            tone = heroTone,
            contentPadding = PaddingValues(Dimensions.SpacingXLarge)
        ) {
            ZenTileCaption(state.title)
            ZenTileValue(
                text = stringResource(R.string.amount_rupees, state.amount),
                style = AmountHero,
                modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
            )
        }

        ZenTile(modifier = Modifier.fillMaxWidth()) {
            DetailRow(label = stringResource(R.string.add_category), value = state.categoryName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(label = stringResource(R.string.txn_wallet), value = state.walletName)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(label = stringResource(R.string.txn_date), value = state.date)
            state.note?.takeIf { it.isNotBlank() }?.let { note ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(label = stringResource(R.string.txn_note), value = note)
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
