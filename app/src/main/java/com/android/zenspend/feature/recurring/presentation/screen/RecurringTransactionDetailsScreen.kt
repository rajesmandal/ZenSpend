package com.android.zenspend.feature.recurring.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSecondaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import com.android.zenspend.feature.recurring.presentation.viewmodel.RecurringViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionDetailsScreen(
    recurringId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rule = state.recurringTransactions.find { it.id == recurringId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.recurring_details_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(recurringId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = {
                        viewModel.handleEvent(RecurringUiEvent.ToggleActive(recurringId, rule?.isActive ?: true))
                    }) {
                        val icon = if (rule?.isActive == true) Icons.Default.Pause else Icons.Default.PlayArrow
                        Icon(icon, contentDescription = stringResource(R.string.recurring_toggle_status))
                    }
                    IconButton(onClick = {
                        viewModel.handleEvent(RecurringUiEvent.ToggleActive(recurringId, true)) // Simplified: should be archive
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_archive),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (rule == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.recurring_not_found))
            }
        } else {
            val nextDate = remember(rule.nextExecutionDate) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(rule.nextExecutionDate))
            }
            val isIncome = rule.type == TransactionType.INCOME
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(PaddingValues(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingLarge)),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                ZenTile(
                    modifier = Modifier.fillMaxWidth(),
                    tone = if (isIncome) ZenTone.Lime else ZenTone.Ink,
                    contentPadding = PaddingValues(Dimensions.SpacingXLarge)
                ) {
                    ZenTileCaption(rule.name)
                    val template = if (isIncome) R.string.amount_rupees_income else R.string.amount_rupees_expense
                    ZenTileValue(text = stringResource(template, formatMinorUnits(rule.amount)), style = AmountHero)
                }

                ZenTile(modifier = Modifier.fillMaxWidth()) {
                    DetailRow(stringResource(R.string.recurring_frequency), rule.recurrenceType.name)
                    DetailRow(stringResource(R.string.recurring_next_occurrence), nextDate)
                    DetailRow(
                        stringResource(R.string.recurring_status),
                        stringResource(if (rule.isActive) R.string.recurring_active else R.string.recurring_paused)
                    )
                }

                Box(modifier = Modifier.weight(1f))

                if (rule.isActive) {
                    ZenSecondaryButton(
                        text = stringResource(R.string.recurring_pause),
                        onClick = { viewModel.handleEvent(RecurringUiEvent.ToggleActive(rule.id, rule.isActive)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ZenPrimaryButton(
                        text = stringResource(R.string.recurring_resume),
                        onClick = { viewModel.handleEvent(RecurringUiEvent.ToggleActive(rule.id, rule.isActive)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.SpacingSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ZenTileCaption(label)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
