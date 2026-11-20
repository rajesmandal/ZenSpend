package com.android.zenspend.feature.split.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSegmentedControl
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.split.domain.model.SplitMath
import com.android.zenspend.feature.split.domain.model.SplitType
import com.android.zenspend.feature.split.presentation.contract.SplitExpenseFormUiEffect
import com.android.zenspend.feature.split.presentation.contract.SplitExpenseFormUiEvent
import com.android.zenspend.feature.split.presentation.viewmodel.SplitExpenseFormViewModel
import kotlinx.coroutines.flow.collectLatest

private val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
private const val MINOR_PER_MAJOR = 100

/** Add a bill to [groupId] ([expenseId] = 0) or edit an existing one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpenseFormScreen(
    groupId: Long,
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SplitExpenseFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(groupId, expenseId) { viewModel.handleEvent(SplitExpenseFormUiEvent.Init(groupId, expenseId)) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { if (it == SplitExpenseFormUiEffect.NavigateBack) onNavigateBack() }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.handleEvent(SplitExpenseFormUiEvent.Date(it)) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    val amountMinor = state.amountText.trim().toDoubleOrNull()?.takeIf { it > 0 }?.let { Math.round(it * MINOR_PER_MAJOR) } ?: 0L
    val chosen = state.members.map { it.id }.filter { it in state.selected }
    val equalShares = if (amountMinor > 0) SplitMath.equalShares(amountMinor, chosen) else emptyMap()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = if (state.isEdit) {
                    stringResource(R.string.split_form_title_edit)
                } else {
                    stringResource(R.string.split_form_title_add, state.groupName)
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
                onValueChange = { viewModel.handleEvent(SplitExpenseFormUiEvent.Title(it)) },
                label = stringResource(R.string.split_form_what)
            )
            ZenTextField(
                value = state.amountText,
                onValueChange = { viewModel.handleEvent(SplitExpenseFormUiEvent.Amount(it)) },
                label = stringResource(R.string.split_form_amount),
                keyboardOptions = decimalKeyboard
            )

            Label(stringResource(R.string.split_form_paid_by))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                state.members.forEach { m ->
                    FilterChip(
                        selected = state.paidBy == m.id,
                        onClick = { viewModel.handleEvent(SplitExpenseFormUiEvent.PaidBy(m.id)) },
                        label = { Text(m.name) }
                    )
                }
            }

            Label(stringResource(R.string.split_form_split))
            ZenSegmentedControl(
                options = listOf(stringResource(R.string.split_type_equal), stringResource(R.string.split_type_exact)),
                selectedIndex = if (state.splitType == SplitType.EQUAL) 0 else 1,
                onSelected = {
                    viewModel.handleEvent(SplitExpenseFormUiEvent.Type(if (it == 0) SplitType.EQUAL else SplitType.EXACT))
                }
            )

            if (state.splitType == SplitType.EQUAL) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.split_form_who),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.handleEvent(SplitExpenseFormUiEvent.SelectAll) }) {
                        Text(stringResource(R.string.split_form_everyone))
                    }
                }
                state.members.forEach { m ->
                    val checked = m.id in state.selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.handleEvent(SplitExpenseFormUiEvent.Toggle(m.id)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { viewModel.handleEvent(SplitExpenseFormUiEvent.Toggle(m.id)) })
                        Text(m.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        equalShares[m.id]?.let {
                            Text(
                                stringResource(R.string.split_form_each_pays, rupees(it)),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.split_form_exact_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.members.forEach { m ->
                    ZenTextField(
                        value = state.exactTexts[m.id].orEmpty(),
                        onValueChange = { viewModel.handleEvent(SplitExpenseFormUiEvent.Exact(m.id, it)) },
                        label = m.name,
                        keyboardOptions = decimalKeyboard
                    )
                }
                val assigned = state.members.sumOf {
                    (state.exactTexts[it.id]?.trim()?.toDoubleOrNull() ?: 0.0).let { v -> if (v > 0) Math.round(v * MINOR_PER_MAJOR) else 0L }
                }
                if (amountMinor > 0 && assigned != amountMinor) {
                    Text(
                        stringResource(R.string.split_form_exact_left, rupees(kotlin.math.abs(amountMinor - assigned))),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                    Text(stringResource(R.string.split_form_date), style = MaterialTheme.typography.labelMedium)
                    Text(state.dateLabel, style = MaterialTheme.typography.bodyLarge)
                }
            }
            ZenTextField(
                value = state.note,
                onValueChange = { viewModel.handleEvent(SplitExpenseFormUiEvent.Note(it)) },
                label = stringResource(R.string.split_form_note)
            )
            state.errorRes?.let { res ->
                val message = if (state.errorArg != null) stringResource(res, state.errorArg!!) else stringResource(res)
                ZenStatusBanner(message = message, isError = true)
            }
            ZenPrimaryButton(
                text = stringResource(R.string.split_form_save),
                onClick = { viewModel.handleEvent(SplitExpenseFormUiEvent.Save) },
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}
