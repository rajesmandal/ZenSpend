package com.android.zenspend.feature.split.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
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
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.split.domain.model.SplitGroupOverview
import com.android.zenspend.feature.split.presentation.contract.SplitGroupsUiEvent
import com.android.zenspend.feature.split.presentation.viewmodel.SplitGroupsViewModel

/** Every group you share costs with (trip, PG, flat), with what you are owed or owe in each. */
@Composable
fun SplitGroupsScreen(
    onNavigateBack: () -> Unit,
    onGroupClick: (Long) -> Unit,
    viewModel: SplitGroupsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    val myName = stringResource(R.string.split_you)

    if (showCreate) {
        CreateGroupDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, others -> viewModel.handleEvent(SplitGroupsUiEvent.CreateGroup(name, myName, others)) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.split_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.split_add_group))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.groups.isEmpty() -> EmptyState(
                title = stringResource(R.string.split_empty_title),
                message = stringResource(R.string.split_empty_body),
                icon = Icons.Default.Groups,
                action = { ZenPrimaryButton(text = stringResource(R.string.split_add_group), onClick = { showCreate = true }) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                items(state.groups, key = { it.group.id }) { overview ->
                    GroupItem(overview = overview, onClick = { onGroupClick(overview.group.id) })
                }
            }
        }
    }
}

@Composable
private fun GroupItem(overview: SplitGroupOverview, onClick: () -> Unit) {
    val balance = overview.myBalance
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenBadge(label = overview.group.name, icon = Icons.Default.Groups)
            Column(modifier = Modifier.weight(1f)) {
                Text(overview.group.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                ZenTileCaption(
                    stringResource(R.string.split_group_meta, overview.memberCount, rupees(overview.details.totalSpent))
                )
            }
        }
        Text(
            text = when {
                balance > 0 -> stringResource(R.string.split_group_owed_to_you, rupees(balance))
                balance < 0 -> stringResource(R.string.split_group_you_owe, rupees(-balance))
                else -> stringResource(R.string.split_group_settled)
            },
            style = MaterialTheme.typography.labelLarge,
            color = when {
                balance > 0 -> ZenSpendTheme.colors.income
                balance < 0 -> ZenSpendTheme.colors.expense
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(top = Dimensions.SpacingSmall)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (name: String, others: List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var member by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(listOf<String>()) }

    fun commitMember() {
        val clean = member.trim()
        if (clean.isNotEmpty()) members = members + clean
        member = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.split_add_group)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
                ZenTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.split_group_name))
                Text(
                    stringResource(R.string.split_members_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZenTextField(
                        value = member,
                        onValueChange = { member = it },
                        label = stringResource(R.string.split_member_name),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = ::commitMember, enabled = member.isNotBlank()) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.split_add_member))
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    members.forEachIndexed { index, m ->
                        InputChip(
                            selected = false,
                            onClick = { members = members.filterIndexed { i, _ -> i != index } },
                            label = { Text(m) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    // A name typed but not yet added with + still counts.
                    onCreate(name, members + listOfNotNull(member.trim().ifEmpty { null }))
                    onDismiss()
                }
            ) { Text(stringResource(R.string.split_create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
