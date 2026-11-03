package com.android.zenspend.feature.sms.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.designsystem.component.CategorySuggestionChip
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftReviewEvent
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDraftReviewScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SmsDraftReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { viewModel.onEvent(SmsDraftReviewEvent.TitleChanged(it)) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = state.amount,
                        onValueChange = { viewModel.onEvent(SmsDraftReviewEvent.AmountChanged(it)) },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Transaction Type", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val types = listOf(
                        TransactionType.EXPENSE to "Debit",
                        TransactionType.INCOME to "Credit",
                        TransactionType.REFUND to "Refund",
                        TransactionType.CREDIT_CARD_PAYMENT to "Card Bill"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.forEach { (type, label) ->
                            FilterChip(
                                selected = state.type == type,
                                onClick = { viewModel.onEvent(SmsDraftReviewEvent.TypeChanged(type)) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                item {
                    val isCardPayment = state.type == TransactionType.CREDIT_CARD_PAYMENT
                    val label = when (state.type) {
                        TransactionType.EXPENSE -> "Paid from (Bank / Card)"
                        TransactionType.CREDIT_CARD_PAYMENT -> "Paid from (Bank)"
                        else -> "Credited to (Bank / Card)"
                    }
                    Text(label, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val walletOptions = if (isCardPayment) {
                        state.wallets.filter { it.type != WalletType.CREDIT_CARD }
                    } else state.wallets
                    walletOptions.forEach { wallet ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.selectedWalletId == wallet.id,
                                onClick = { viewModel.onEvent(SmsDraftReviewEvent.WalletSelected(wallet.id)) }
                            )
                            val kind = if (wallet.type == WalletType.CREDIT_CARD) "Card" else "Bank/Wallet"
                            Text("${wallet.name} ($kind)")
                        }
                    }
                }

                if (state.type == TransactionType.CREDIT_CARD_PAYMENT) {
                    item {
                        Text("Credit Card paid", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.wallets.filter { it.type == WalletType.CREDIT_CARD }.forEach { wallet ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.selectedDestinationWalletId == wallet.id,
                                    onClick = { viewModel.onEvent(SmsDraftReviewEvent.DestinationWalletSelected(wallet.id)) }
                                )
                                Text(wallet.name)
                            }
                        }
                    }
                }

                item {
                    Text("Select Category", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.categorySuggestion != null) {
                        CategorySuggestionChip(
                            suggestion = state.categorySuggestion!!,
                            onClick = { viewModel.onEvent(SmsDraftReviewEvent.CategorySelected(state.categorySuggestion!!.categoryId)) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    state.categories.forEach { category ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.selectedCategoryId == category.id,
                                onClick = { viewModel.onEvent(SmsDraftReviewEvent.CategorySelected(category.id)) }
                            )
                            Text(category.name)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.onEvent(SmsDraftReviewEvent.ConfirmClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.selectedWalletId != null &&
                            (state.type != TransactionType.CREDIT_CARD_PAYMENT || state.selectedDestinationWalletId != null)
                    ) {
                        Text("Confirm & Add Transaction")
                    }
                }
                
                if (state.error != null) {
                    item {
                        Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
