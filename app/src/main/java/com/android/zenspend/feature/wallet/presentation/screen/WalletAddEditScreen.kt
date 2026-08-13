package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEffect
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEvent
import com.android.zenspend.feature.wallet.presentation.viewmodel.WalletViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletAddEditScreen(
    onNavigateBack: () -> Unit,
    walletId: Long? = null,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.formState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(walletId) {
        if (walletId != null && walletId != 0L) {
            viewModel.handleEvent(WalletUiEvent.LoadWalletForEdit(walletId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is WalletUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is WalletUiEffect.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditMode) "Edit Wallet" else "Add Wallet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            ZenTextField(
                value = form.name,
                onValueChange = { viewModel.handleEvent(WalletUiEvent.OnNameChange(it)) },
                label = "Wallet Name",
                isError = form.nameError != null
            )
            if (form.nameError != null) {
                Text(form.nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            TypeSelector(
                selectedType = form.type,
                onTypeSelected = { viewModel.handleEvent(WalletUiEvent.OnTypeChange(it)) },
                enabled = !form.isEditMode
            )

            if (!form.isEditMode) {
                Text("Opening Balance", style = MaterialTheme.typography.labelMedium)
                AmountInput(
                    value = form.openingBalanceText,
                    onValueChange = { viewModel.handleEvent(WalletUiEvent.OnBalanceChange(it)) },
                    isError = form.balanceError != null
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ZenPrimaryButton(
                text = if (form.isEditMode) "Update Wallet" else "Create Wallet",
                onClick = { viewModel.handleEvent(WalletUiEvent.SaveWallet) },
                loading = form.isSubmitting
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSelector(
    selectedType: WalletType,
    onTypeSelected: (WalletType) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text("Wallet Type", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = enabled
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                WalletType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
