package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEffect
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEvent
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionAddEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.formState
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showVoiceOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is TransactionUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is TransactionUiEffect.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!form.isEditMode) {
                        IconButton(onClick = { showVoiceOverlay = true }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            TransactionTypeTabSelector(
                selectedType = form.type,
                onTypeSelected = { viewModel.handleEvent(TransactionUiEvent.OnTypeChange(it)) }
            )

            AmountInput(
                value = form.amountText,
                onValueChange = { viewModel.handleEvent(TransactionUiEvent.OnAmountChange(it)) },
                isError = form.amountError != null
            )

            ZenTextField(
                value = form.title,
                onValueChange = { viewModel.handleEvent(TransactionUiEvent.OnTitleChange(it)) },
                label = "Title (e.g. Lunch, Amazon)"
            )

            // Dynamic Wallet Selectors
            when (form.type) {
                TransactionType.EXPENSE, TransactionType.REFUND -> {
                    WalletSelector(
                        label = if (form.type == TransactionType.EXPENSE) "From Wallet" else "To Wallet",
                        selectedId = form.sourceWalletId,
                        wallets = state.wallets,
                        onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnSourceWalletChange(it)) }
                    )
                }
                TransactionType.INCOME -> {
                    WalletSelector(
                        label = "To Wallet",
                        selectedId = form.destinationWalletId ?: 0L,
                        wallets = state.wallets,
                        onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnDestinationWalletChange(it)) }
                    )
                }
                TransactionType.TRANSFER, TransactionType.CREDIT_CARD_PAYMENT -> {
                    WalletSelector(
                        label = "From Wallet",
                        selectedId = form.sourceWalletId,
                        wallets = state.wallets,
                        onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnSourceWalletChange(it)) }
                    )
                    WalletSelector(
                        label = if (form.type == TransactionType.TRANSFER) "To Wallet" else "To Credit Card",
                        selectedId = form.destinationWalletId ?: 0L,
                        wallets = if (form.type == TransactionType.CREDIT_CARD_PAYMENT) 
                            state.wallets.filter { it.type == com.android.zenspend.core.database.model.WalletType.CREDIT_CARD }
                            else state.wallets,
                        onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnDestinationWalletChange(it)) }
                    )
                }
            }

            if (form.type == TransactionType.EXPENSE || form.type == TransactionType.INCOME) {
                if (state.categorySuggestion != null) {
                    CategorySuggestionChip(
                        suggestion = state.categorySuggestion!!,
                        onClick = { viewModel.handleEvent(TransactionUiEvent.OnCategoryChange(state.categorySuggestion!!.categoryId)) }
                    )
                }
                
                CategorySelector(
                    selectedId = form.categoryId,
                    categories = state.categories,
                    onCategorySelected = { viewModel.handleEvent(TransactionUiEvent.OnCategoryChange(it)) }
                )
            }

            ZenTextField(
                value = form.note,
                onValueChange = { viewModel.handleEvent(TransactionUiEvent.OnNoteChange(it)) },
                label = "Note (Optional)"
            )

            Spacer(modifier = Modifier.weight(1f))

            ZenPrimaryButton(
                text = "Save Transaction",
                onClick = { viewModel.handleEvent(TransactionUiEvent.SaveTransaction) },
                loading = form.isSubmitting
            )
        }

        if (showVoiceOverlay) {
            com.android.zenspend.feature.voice.presentation.components.VoiceInputOverlay(
                onDismiss = { showVoiceOverlay = false },
                onDraftAccepted = { draft ->
                    viewModel.handleEvent(TransactionUiEvent.OnVoiceDraftAccepted(draft))
                    showVoiceOverlay = false
                },
                viewModel = hiltViewModel()
            )
        }
    }
}

@Composable
fun TransactionTypeTabSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedType.ordinal,
        edgePadding = 0.dp,
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        divider = {}
    ) {
        TransactionType.values().forEach { type ->
            Tab(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                text = { Text(type.name, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelector(
    label: String,
    selectedId: Long,
    wallets: List<com.android.zenspend.feature.wallet.presentation.model.WalletUiModel>,
    onWalletSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedWallet = wallets.find { it.id == selectedId }

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedWallet?.name ?: "Select Wallet",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = { Text("${wallet.name} (₹${wallet.balance})") },
                        onClick = {
                            onWalletSelected(wallet.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedId: Long?,
    categories: List<com.android.zenspend.feature.category.domain.model.Category>,
    onCategorySelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedId }

    Column {
        Text("Category", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
