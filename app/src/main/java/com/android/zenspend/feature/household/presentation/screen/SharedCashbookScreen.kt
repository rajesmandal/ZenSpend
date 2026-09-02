package com.android.zenspend.feature.household.presentation.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.household.domain.model.HouseholdMember
import com.android.zenspend.feature.household.domain.model.HouseholdRole
import com.android.zenspend.feature.household.domain.model.Invitation
import com.android.zenspend.feature.household.presentation.viewmodel.HouseholdViewModel

@Composable
fun SharedCashbookScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: HouseholdViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.authState !is AuthState.Authenticated) {
        SharedCashbookSignInPrompt(onSignIn = onNavigateToLogin, onBack = onNavigateBack)
        return
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Shared Cashbook",
                subtitle = "Manage household finances together",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
        ) {
            state.errorMessage?.let { message ->
                item { ZenStatusBanner(message = message, isError = true) }
            }
            state.successMessage?.let { message ->
                item { ZenStatusBanner(message = message) }
            }

            if (state.household == null) {
                item {
                    PremiumFeatureGate(
                        entitlementManager = viewModel.entitlementManager,
                        onUpgradeClick = onNavigateToPremium
                    ) {
                        CreateHouseholdSection(
                            name = state.householdName,
                            onNameChange = viewModel::onHouseholdNameChange,
                            onCreate = viewModel::createHousehold,
                            isLoading = state.isLoading
                        )
                    }
                }
                item {
                    JoinHouseholdSection(
                        token = state.inviteToken,
                        onTokenChange = viewModel::onInviteTokenChange,
                        onJoin = viewModel::joinHousehold,
                        isLoading = state.isLoading
                    )
                }
            } else {
                item {
                    HouseholdHeaderCard(householdName = state.household!!.name)
                }
                item {
                    Text("Members", style = MaterialTheme.typography.titleMedium)
                }
                items(state.members) { member ->
                    ZenMemberChip(
                        displayName = member.displayName,
                        role = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        isCurrentUser = member.userId == (state.authState as AuthState.Authenticated).userId
                    )
                }
                item {
                    HouseholdActions(
                        currentMember = state.members.find {
                            it.userId == (state.authState as AuthState.Authenticated).userId
                        },
                        onInvite = viewModel::createInvitation,
                        onLeave = viewModel::leaveHousehold
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(Dimensions.SpacingLarge)) }
        }
    }

    LaunchedEffect(state.lastInvitation) {
        state.lastInvitation?.let { invitation ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invite to ZenSpend")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Join my ZenSpend shared cashbook:\nzenspend://join?token=${invitation.token}"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Invite to ZenSpend"))
        }
    }
}

@Composable
private fun SharedCashbookSignInPrompt(onSignIn: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Shared Cashbook",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.ScreenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Sign in to create or join a shared cashbook with your partner or family.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(Dimensions.SectionGap))
            ZenPrimaryButton(text = "Sign In", onClick = onSignIn)
        }
    }
}

@Composable
private fun CreateHouseholdSection(
    name: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    isLoading: Boolean
) {
    ZenSurfaceCard {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Dimensions.SpacingMedium))
            Text("Create Cashbook", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Household name",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenPrimaryButton(
            text = if (isLoading) "Creating…" else "Create Cashbook",
            onClick = onCreate,
            enabled = !isLoading && name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun JoinHouseholdSection(
    token: String,
    onTokenChange: (String) -> Unit,
    onJoin: () -> Unit,
    isLoading: Boolean
) {
    ZenSurfaceCard {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Dimensions.SpacingMedium))
            Text("Join Cashbook", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenTextField(
            value = token,
            onValueChange = onTokenChange,
            label = "Invitation token",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenSecondaryButton(
            text = if (isLoading) "Joining…" else "Join with Token",
            onClick = onJoin,
            enabled = !isLoading && token.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HouseholdHeaderCard(householdName: String) {
    ZenSurfaceCard {
        Text("Our Household", style = MaterialTheme.typography.titleLarge)
        Text(
            householdName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HouseholdActions(
    currentMember: HouseholdMember?,
    onInvite: () -> Unit,
    onLeave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
        if (currentMember?.role == HouseholdRole.OWNER) {
            ZenPrimaryButton(
                text = "Invite Partner",
                onClick = onInvite,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Share
            )
            Text(
                "Owner cannot leave until ownership is transferred.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ZenSecondaryButton(
                text = "Leave Household",
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SharedCashbookPreview() {
    ZenSpendTheme {
        Column(Modifier.padding(16.dp)) {
            ZenMemberChip(displayName = "Alex", role = "Owner", isCurrentUser = true)
            Spacer(Modifier.height(8.dp))
            ZenMemberChip(displayName = "Sam", role = "Member", isCurrentUser = false)
        }
    }
}
