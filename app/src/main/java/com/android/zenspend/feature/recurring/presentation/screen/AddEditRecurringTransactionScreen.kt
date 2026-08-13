package com.android.zenspend.feature.recurring.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import com.android.zenspend.feature.recurring.presentation.viewmodel.RecurringViewModel
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEvent
import com.android.zenspend.feature.wallet.presentation.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    
    val form = state.formState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditMode) "Edit Rule" else "New Recurring Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.SpacingLarge)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
        ) {
            // Type Selector
            Row(modifier = Modifier.fillMaxWidth()) {
                TransactionType.values().filter { it != TransactionType.REFUND && it != TransactionType.CREDIT_CARD_PAYMENT }.forEach { type ->
                    FilterChip(
                        selected = form.type == type,
                        onClick = { viewModel.handleEvent(RecurringUiEvent.OnTypeChange(type)) },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(end = Dimensions.SpacingSmall)
                    )
                }
            }

            ZenTextField(
                value = form.name,
                onValueChange = { viewModel.handleEvent(RecurringUiEvent.OnNameChange(it)) },
                label = "Name (e.g. Netflix, Rent)",
                modifier = Modifier.fillMaxWidth()
            )

            AmountInput(
                value = form.amountText,
                onValueChange = { viewModel.handleEvent(RecurringUiEvent.OnAmountChange(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency
            Text("Frequency", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth()) {
                RecurrenceType.entries.forEach { freq ->
                    FilterChip(
                        selected = form.frequency == freq,
                        onClick = { viewModel.handleEvent(RecurringUiEvent.OnFrequencyChange(freq)) },
                        label = { Text(freq.name) },
                        modifier = Modifier.padding(end = Dimensions.SpacingSmall)
                    )
                }
            }

            // Wallet Selectors based on type
            if (form.type == TransactionType.EXPENSE || form.type == TransactionType.TRANSFER) {
                Text("From Wallet", style = MaterialTheme.typography.labelLarge)
                walletState.wallets.forEach { wallet ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = form.sourceWalletId == wallet.id,
                            onClick = { viewModel.handleEvent(RecurringUiEvent.OnSourceWalletChange(wallet.id)) }
                        )
                        Text(wallet.name)
                    }
                }
            }

            if (form.type == TransactionType.INCOME || form.type == TransactionType.TRANSFER) {
                Text("To Wallet", style = MaterialTheme.typography.labelLarge)
                walletState.wallets.forEach { wallet ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = form.destinationWalletId == wallet.id,
                            onClick = { viewModel.handleEvent(RecurringUiEvent.OnDestinationWalletChange(wallet.id)) }
                        )
                        Text(wallet.name)
                    }
                }
            }

            ZenPrimaryButton(
                text = "Save Rule",
                onClick = { viewModel.handleEvent(RecurringUiEvent.SaveRecurring) },
                modifier = Modifier.fillMaxWidth(),
                loading = form.isSubmitting
            )
        }
    }
}
