package com.android.zenspend.feature.emi.presentation.screen

import com.android.zenspend.R
import androidx.compose.ui.res.stringResource
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
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEffect
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEvent
import com.android.zenspend.feature.emi.presentation.viewmodel.AddEMIViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEMIScreen(
    creditCardId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddEMIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(creditCardId) {
        viewModel.handleEvent(AddEMIUiEvent.Init(creditCardId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is AddEMIUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                AddEMIUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.startDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.handleEvent(AddEMIUiEvent.OnStartDateChange(millis))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.cards_add_emi),
                subtitle = stringResource(R.string.emi_form_subtitle),
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
            ZenSurfaceCard {
                Text(stringResource(R.string.emi_form_linked_card), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(Dimensions.SpacingXSmall))
                Text(state.linkedCardLabel, style = MaterialTheme.typography.titleMedium)
            }

            ZenTextField(
                value = state.name,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnNameChange(it)) },
                label = stringResource(R.string.emi_form_name)
            )

            AmountInput(
                value = state.principalText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnPrincipalChange(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            AmountInput(
                value = state.emiAmountText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnEmiAmountChange(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(
                    value = state.tenureText,
                    onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnTenureChange(it)) },
                    label = stringResource(R.string.emi_form_tenure),
                    modifier = Modifier.weight(1f)
                )
                ZenTextField(
                    value = state.dueDayText,
                    onValueChange = {
                        if (it.length <= 2) viewModel.handleEvent(AddEMIUiEvent.OnDueDayChange(it))
                    },
                    label = stringResource(R.string.emi_form_due_day),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.SpacingSmall)) {
                    Text(stringResource(R.string.emi_form_start_date), style = MaterialTheme.typography.labelMedium)
                    Text(
                        state.startDateLabel.ifBlank { stringResource(R.string.emi_form_select_date) },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            ZenTextField(
                value = state.interestRateText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnInterestRateChange(it)) },
                label = stringResource(R.string.emi_form_interest)
            )

            state.error?.let { error ->
                ZenStatusBanner(message = error, isError = true)
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            ZenPrimaryButton(
                text = stringResource(R.string.emi_form_save),
                onClick = { viewModel.handleEvent(AddEMIUiEvent.Save) },
                loading = state.isSubmitting
            )
        }
    }
}
