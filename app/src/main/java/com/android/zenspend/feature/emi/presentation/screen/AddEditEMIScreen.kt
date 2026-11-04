package com.android.zenspend.feature.emi.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEffect
import com.android.zenspend.feature.emi.presentation.contract.AddEMIUiEvent
import com.android.zenspend.feature.emi.presentation.viewmodel.AddEMIViewModel
import kotlinx.coroutines.flow.collectLatest

private val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
private val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

/** Add a new EMI ([emiId] = 0) or edit an existing one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEMIScreen(
    creditCardId: Long,
    emiId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddEMIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(creditCardId, emiId) {
        viewModel.handleEvent(AddEMIUiEvent.Init(creditCardId, emiId))
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.handleEvent(AddEMIUiEvent.OnStartDateChange(it)) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    val lenderTypes = EMILenderType.entries
    val lenderLabels = lenderTypes.map { stringResource(it.labelRes()) }
    val isCard = state.lenderType == EMILenderType.CREDIT_CARD

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(if (state.isEdit) R.string.emi_form_title_edit else R.string.emi_form_title_add),
                subtitle = stringResource(R.string.emi_form_subtitle_new),
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
            ZenSectionHeader(title = stringResource(R.string.emi_form_lender_type))
            ZenChipRow(
                options = lenderLabels,
                selectedIndex = lenderTypes.indexOf(state.lenderType),
                onSelected = { viewModel.handleEvent(AddEMIUiEvent.OnLenderTypeChange(lenderTypes[it])) },
                contentPadding = PaddingValues()
            )

            if (isCard) {
                if (state.cards.isEmpty()) {
                    ZenStatusBanner(message = stringResource(R.string.emi_form_no_cards))
                } else {
                    ZenSectionHeader(title = stringResource(R.string.emi_form_card))
                    ZenChipRow(
                        options = state.cards.map { it.label },
                        selectedIndex = state.cards.indexOfFirst { it.id == state.selectedCardId },
                        onSelected = { viewModel.handleEvent(AddEMIUiEvent.OnCardSelected(state.cards[it].id)) },
                        contentPadding = PaddingValues()
                    )
                }
            } else {
                ZenTextField(
                    value = state.lenderName,
                    onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnLenderNameChange(it)) },
                    label = stringResource(
                        when (state.lenderType) {
                            EMILenderType.PERSON -> R.string.emi_form_lender_person
                            EMILenderType.BANK -> R.string.emi_form_lender_bank
                            else -> R.string.emi_form_lender_other
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ZenTextField(
                value = state.name,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnNameChange(it)) },
                label = stringResource(R.string.emi_form_name),
                modifier = Modifier.fillMaxWidth()
            )
            ZenTextField(
                value = state.principalText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnPrincipalChange(it)) },
                label = stringResource(R.string.emi_form_principal),
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth()
            )
            ZenTextField(
                value = state.emiAmountText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnEmiAmountChange(it)) },
                label = stringResource(R.string.emi_form_emi_amount),
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(
                    value = state.tenureText,
                    onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnTenureChange(it)) },
                    label = stringResource(R.string.emi_form_tenure),
                    keyboardOptions = numberKeyboard,
                    modifier = Modifier.weight(1f)
                )
                ZenTextField(
                    value = state.dueDayText,
                    onValueChange = { if (it.length <= DUE_DAY_MAX_DIGITS) viewModel.handleEvent(AddEMIUiEvent.OnDueDayChange(it)) },
                    label = stringResource(R.string.emi_form_due_day),
                    keyboardOptions = numberKeyboard,
                    modifier = Modifier.weight(1f)
                )
            }

            ZenTextField(
                value = state.paidText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnPaidChange(it)) },
                label = stringResource(R.string.emi_form_paid),
                keyboardOptions = numberKeyboard,
                modifier = Modifier.fillMaxWidth()
            )
            if (!state.isEdit) {
                Text(
                    stringResource(R.string.emi_form_paid_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = stringResource(R.string.emi_form_interest),
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth()
            )
            ZenTextField(
                value = state.noteText,
                onValueChange = { viewModel.handleEvent(AddEMIUiEvent.OnNoteChange(it)) },
                label = stringResource(R.string.emi_form_note),
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { ZenStatusBanner(message = it, isError = true) }

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            ZenPrimaryButton(
                text = stringResource(R.string.emi_form_save),
                onClick = { viewModel.handleEvent(AddEMIUiEvent.Save) },
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val DUE_DAY_MAX_DIGITS = 2
