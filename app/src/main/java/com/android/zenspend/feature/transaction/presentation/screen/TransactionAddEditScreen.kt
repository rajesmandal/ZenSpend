package com.android.zenspend.feature.transaction.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.designsystem.component.CategorySuggestionChip
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenKeypad
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.applyKeypadKey
import com.android.zenspend.core.ui.theme.AmountDisplay
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.InputShape
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEffect
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEvent
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(
                    if (form.isEditMode) R.string.edit_transaction_title else R.string.add_transaction_title
                ),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (!form.isEditMode) {
                        IconButton(onClick = { showVoiceOverlay = true }) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = stringResource(R.string.action_voice_input)
                            )
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
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimensions.SpacingXLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
        ) {
            // Type selector
            val types = TransactionType.entries
            ZenChipRow(
                options = types.map { stringResource(it.labelRes()) },
                selectedIndex = types.indexOf(form.type),
                onSelected = { viewModel.handleEvent(TransactionUiEvent.OnTypeChange(types[it])) }
            )

            Column(
                modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                // Big amount + keypad
                AmountReadout(
                    amountText = form.amountText,
                    isError = form.amountError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                ZenKeypad(
                    onKey = { key ->
                        viewModel.handleEvent(
                            TransactionUiEvent.OnAmountChange(applyKeypadKey(form.amountText, key))
                        )
                    }
                )

                ZenTextField(
                    value = form.title,
                    onValueChange = { viewModel.handleEvent(TransactionUiEvent.OnTitleChange(it)) },
                    label = stringResource(R.string.add_field_title)
                )

                // Dynamic wallet selectors
                val fromLabel = stringResource(R.string.add_from_wallet)
                val toLabel = stringResource(R.string.add_to_wallet)
                when (form.type) {
                    TransactionType.EXPENSE -> {
                        WalletSelector(
                            label = fromLabel,
                            selectedId = form.sourceWalletId,
                            wallets = state.wallets,
                            onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnSourceWalletChange(it)) }
                        )
                    }
                    TransactionType.INCOME, TransactionType.REFUND -> {
                        WalletSelector(
                            label = toLabel,
                            selectedId = form.destinationWalletId ?: 0L,
                            wallets = state.wallets,
                            onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnDestinationWalletChange(it)) }
                        )
                    }
                    TransactionType.TRANSFER, TransactionType.CREDIT_CARD_PAYMENT -> {
                        WalletSelector(
                            label = fromLabel,
                            selectedId = form.sourceWalletId,
                            wallets = state.wallets,
                            onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnSourceWalletChange(it)) }
                        )
                        WalletSelector(
                            label = if (form.type == TransactionType.TRANSFER) {
                                toLabel
                            } else {
                                stringResource(R.string.add_to_credit_card)
                            },
                            selectedId = form.destinationWalletId ?: 0L,
                            wallets = if (form.type == TransactionType.CREDIT_CARD_PAYMENT) {
                                state.wallets.filter { it.type == WalletType.CREDIT_CARD }
                            } else {
                                state.wallets
                            },
                            onWalletSelected = { viewModel.handleEvent(TransactionUiEvent.OnDestinationWalletChange(it)) }
                        )
                    }
                }

                if (form.type == TransactionType.EXPENSE || form.type == TransactionType.INCOME) {
                    state.categorySuggestion?.let { suggestion ->
                        CategorySuggestionChip(
                            suggestion = suggestion,
                            onClick = {
                                viewModel.handleEvent(TransactionUiEvent.OnCategoryChange(suggestion.categoryId))
                            }
                        )
                    }

                    CategoryChips(
                        selectedId = form.categoryId,
                        categories = state.categories,
                        onCategorySelected = { viewModel.handleEvent(TransactionUiEvent.OnCategoryChange(it)) }
                    )
                }

                ZenTextField(
                    value = form.note,
                    onValueChange = { viewModel.handleEvent(TransactionUiEvent.OnNoteChange(it)) },
                    label = stringResource(R.string.add_field_note)
                )

                ZenPrimaryButton(
                    text = stringResource(R.string.add_save),
                    onClick = { viewModel.handleEvent(TransactionUiEvent.SaveTransaction) },
                    loading = form.isSubmitting
                )
            }
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

@StringRes
private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.EXPENSE -> R.string.type_expense
    TransactionType.INCOME -> R.string.type_income
    TransactionType.TRANSFER -> R.string.type_transfer
    TransactionType.REFUND -> R.string.type_refund
    TransactionType.CREDIT_CARD_PAYMENT -> R.string.type_card_payment
}

/** Large amount read-out that mirrors what the keypad has typed. */
@Composable
private fun AmountReadout(
    amountText: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val numberColor = when {
        isError -> scheme.error
        amountText.isEmpty() -> scheme.outlineVariant
        else -> scheme.onSurface
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = stringResource(R.string.amount_rupees, ""),
            style = MaterialTheme.typography.headlineLarge,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimensions.SpacingSmall, end = Dimensions.SpacingXSmall)
        )
        Text(
            text = amountText.ifEmpty { stringResource(R.string.add_amount_placeholder) },
            style = AmountDisplay,
            color = numberColor
        )
    }
}

/** Wrapping pill chips for picking a category. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    selectedId: Long?,
    categories: List<com.android.zenspend.feature.category.domain.model.Category>,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
        Text(
            text = stringResource(R.string.add_category),
            style = MaterialTheme.typography.titleLarge
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            categories.forEach { category ->
                ZenChip(
                    label = category.name,
                    selected = category.id == selectedId,
                    onClick = { onCategorySelected(category.id) }
                )
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingXSmall)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedWallet?.name ?: "",
                onValueChange = {},
                readOnly = true,
                shape = InputShape,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${wallet.name} (${stringResource(R.string.amount_rupees, wallet.balance)})"
                            )
                        },
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

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingXSmall)) {
        Text(stringResource(R.string.add_category), style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                shape = InputShape,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
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
