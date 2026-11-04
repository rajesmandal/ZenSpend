package com.android.zenspend.feature.wallet.presentation.screen

import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.android.zenspend.R
import androidx.compose.ui.res.stringResource
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(if (form.isEditMode) R.string.wallet_edit_title else R.string.wallet_add_title),
                showBack = true,
                onBackClick = onNavigateBack
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
                label = stringResource(R.string.wallet_form_name),
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
                Text(stringResource(R.string.wallet_form_opening_balance), style = MaterialTheme.typography.labelMedium)
                AmountInput(
                    value = form.openingBalanceText,
                    onValueChange = { viewModel.handleEvent(WalletUiEvent.OnBalanceChange(it)) },
                    isError = form.balanceError != null
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ZenPrimaryButton(
                text = stringResource(if (form.isEditMode) R.string.wallet_update else R.string.wallet_create),
                onClick = { viewModel.handleEvent(WalletUiEvent.SaveWallet) },
                loading = form.isSubmitting
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeSelector(
    selectedType: WalletType,
    onTypeSelected: (WalletType) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
        Text(stringResource(R.string.wallet_form_type), style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            WalletType.values().forEach { type ->
                ZenChip(
                    label = type.name,
                    selected = type == selectedType,
                    onClick = { if (enabled) onTypeSelected(type) }
                )
            }
        }
    }
}
