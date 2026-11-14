package com.android.zenspend.feature.people.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.people.domain.model.PersonOverview
import com.android.zenspend.feature.people.domain.model.ReceivablesSummary
import com.android.zenspend.feature.people.presentation.contract.PeopleUiEvent
import com.android.zenspend.feature.people.presentation.viewmodel.PeopleViewModel

/** Everyone you have lent money to, with what each still owes you. */
@Composable
fun PeopleScreen(
    onNavigateBack: () -> Unit,
    onPersonClick: (Long) -> Unit,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        AddPersonDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, phone -> viewModel.handleEvent(PeopleUiEvent.AddPerson(name, phone)) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.people_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showAdd = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.people_add))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.people.isEmpty() -> EmptyState(
                title = stringResource(R.string.people_empty_title),
                message = stringResource(R.string.people_empty_body),
                icon = Icons.Default.Person,
                action = { ZenPrimaryButton(text = stringResource(R.string.people_add), onClick = { showAdd = true }) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item { SummaryTile(state.summary) }
                items(state.people, key = { it.person.id }) { overview ->
                    PersonItem(overview = overview, onClick = { onPersonClick(overview.person.id) })
                }
            }
        }
    }
}

@Composable
private fun SummaryTile(summary: ReceivablesSummary) {
    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        ZenTileCaption(stringResource(R.string.people_summary_to_receive))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, formatMinorUnits(summary.remainingTotal)),
            style = AmountHero
        )
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXLarge)) {
            Column {
                ZenTileCaption(stringResource(R.string.people_summary_monthly))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(summary.monthlyTotal)),
                    style = AmountMedium
                )
            }
            Column {
                ZenTileCaption(stringResource(R.string.people_summary_received))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(summary.receivedTotal)),
                    style = AmountMedium,
                    color = ZenSpendTheme.colors.tone(ZenTone.Lime).container
                )
            }
        }
    }
}

@Composable
private fun PersonItem(overview: PersonOverview, onClick: () -> Unit) {
    val nextDue = overview.nextDue
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenBadge(label = overview.person.name, icon = Icons.Default.Person)
            Column(modifier = Modifier.weight(1f)) {
                Text(overview.person.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                ZenTileCaption(
                    if (overview.activeLoanCount > 0) {
                        stringResource(R.string.people_item_loans, overview.activeLoanCount)
                    } else {
                        stringResource(R.string.people_item_settled)
                    }
                )
            }
            if (overview.remaining > 0) {
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(overview.remaining)),
                    style = AmountMedium
                )
            }
        }
        if (nextDue != null) {
            Text(
                text = stringResource(
                    if (overview.hasOverdue) R.string.people_item_overdue else R.string.people_item_next,
                    formatLoanDate(nextDue)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (overview.hasOverdue) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimensions.SpacingSmall)
            )
        }
    }
}

@Composable
private fun AddPersonDialog(onDismiss: () -> Unit, onAdd: (name: String, phone: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.people_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.people_name))
                ZenTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(R.string.people_phone),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onAdd(name, phone); onDismiss() }
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
