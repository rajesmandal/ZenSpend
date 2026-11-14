package com.android.zenspend.feature.people.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.people.presentation.contract.LoanFormUiEffect
import com.android.zenspend.feature.people.presentation.contract.LoanFormUiEvent
import com.android.zenspend.feature.people.presentation.viewmodel.LoanFormViewModel
import kotlinx.coroutines.flow.collectLatest

private val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
private val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)
private const val DUE_DAY_MAX_DIGITS = 2

/** Give a new loan to [personId] ([loanId] = 0) or edit an existing one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanFormScreen(
    personId: Long,
    loanId: Long,
    onNavigateBack: () -> Unit,
    viewModel: LoanFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(personId, loanId) { viewModel.handleEvent(LoanFormUiEvent.Init(personId, loanId)) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { if (it == LoanFormUiEffect.NavigateBack) onNavigateBack() }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.handleEvent(LoanFormUiEvent.StartDate(it)) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = if (state.isEdit) {
                    stringResource(R.string.loan_form_title_edit)
                } else {
                    stringResource(R.string.loan_form_title_add, state.personName)
                },
                showBack = true,
                onBackClick = onNavigateBack
            )
        }
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
                value = state.title,
                onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Title(it)) },
                label = stringResource(R.string.loan_form_name)
            )
            ZenTextField(
                value = state.principalText,
                onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Principal(it)) },
                label = stringResource(R.string.loan_form_principal),
                keyboardOptions = decimalKeyboard
            )
            ZenTextField(
                value = state.installmentText,
                onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Installment(it)) },
                label = stringResource(R.string.loan_form_installment),
                keyboardOptions = decimalKeyboard
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(
                    value = state.tenureText,
                    onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Tenure(it)) },
                    label = stringResource(R.string.loan_form_tenure),
                    keyboardOptions = numberKeyboard,
                    modifier = Modifier.weight(1f)
                )
                ZenTextField(
                    value = state.dueDayText,
                    onValueChange = { if (it.length <= DUE_DAY_MAX_DIGITS) viewModel.handleEvent(LoanFormUiEvent.DueDay(it)) },
                    label = stringResource(R.string.loan_form_due_day),
                    keyboardOptions = numberKeyboard,
                    modifier = Modifier.weight(1f)
                )
            }
            ZenTextField(
                value = state.receivedText,
                onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Received(it)) },
                label = stringResource(R.string.loan_form_received),
                keyboardOptions = numberKeyboard
            )
            if (!state.isEdit) {
                Text(
                    stringResource(R.string.loan_form_received_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SpacingSmall)
                ) {
                    Text(stringResource(R.string.loan_form_start_date), style = MaterialTheme.typography.labelMedium)
                    Text(state.startDateLabel, style = MaterialTheme.typography.bodyLarge)
                }
            }
            ZenTextField(
                value = state.noteText,
                onValueChange = { viewModel.handleEvent(LoanFormUiEvent.Note(it)) },
                label = stringResource(R.string.loan_form_note)
            )
            state.errorRes?.let { ZenStatusBanner(message = stringResource(it), isError = true) }
            ZenPrimaryButton(
                text = stringResource(R.string.loan_form_save),
                onClick = { viewModel.handleEvent(LoanFormUiEvent.Save) },
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
