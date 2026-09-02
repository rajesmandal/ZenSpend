package com.android.zenspend.feature.creditcard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardUiEffect
import com.android.zenspend.feature.creditcard.presentation.contract.CreditCardUiEvent
import com.android.zenspend.feature.creditcard.presentation.viewmodel.CreditCardViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditCreditCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.formState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CreditCardUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is CreditCardUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = "Add Credit Card",
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
                .padding(Dimensions.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
        ) {
            ZenTextField(
                value = form.cardName,
                onValueChange = { viewModel.handleEvent(CreditCardUiEvent.OnCardNameChange(it)) },
                label = "Card Name (e.g. Millennia)"
            )

            ZenTextField(
                value = form.bankName,
                onValueChange = { viewModel.handleEvent(CreditCardUiEvent.OnBankNameChange(it)) },
                label = "Bank Name (e.g. HDFC)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(
                    value = form.lastFourDigits,
                    onValueChange = {
                        if (it.length <= 4) viewModel.handleEvent(CreditCardUiEvent.OnLastFourChange(it))
                    },
                    label = "Last 4 Digits",
                    modifier = Modifier.weight(1f)
                )

                ZenTextField(
                    value = form.billingDay,
                    onValueChange = { viewModel.handleEvent(CreditCardUiEvent.OnBillingDayChange(it)) },
                    label = "Billing Day",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                AmountInput(
                    value = form.creditLimitText,
                    onValueChange = { viewModel.handleEvent(CreditCardUiEvent.OnLimitChange(it)) },
                    modifier = Modifier.weight(1f)
                )
                ZenTextField(
                    value = form.dueDay,
                    onValueChange = { viewModel.handleEvent(CreditCardUiEvent.OnDueDayChange(it)) },
                    label = "Payment Due Day",
                    modifier = Modifier.weight(1f)
                )
            }

            form.error?.let { error ->
                ZenStatusBanner(message = error, isError = true)
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            ZenPrimaryButton(
                text = "Save Card",
                onClick = { viewModel.handleEvent(CreditCardUiEvent.SaveCreditCard) },
                loading = form.isSubmitting
            )
        }
    }
}
