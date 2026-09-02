package com.android.zenspend.feature.settings.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.android.zenspend.core.ui.designsystem.component.ZenSettingsRow
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToBackup: () -> Unit,
    onNavigateToSmsDrafts: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSharedCashbook: () -> Unit,
    onNavigateToCreditCards: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val premiumViewModel: com.android.zenspend.feature.premium.presentation.viewmodel.PremiumViewModel = hiltViewModel()
    val isPremium by premiumViewModel.state.collectAsState()

    var showSmsDisclosure by remember { mutableStateOf(false) }

    if (showSmsDisclosure) {
        AlertDialog(
            onDismissRequest = { showSmsDisclosure = false },
            title = { Text("SMS Transaction Detection") },
            text = {
                Text(
                    "ZenSpend can help you track expenses by detecting transaction SMS messages from your bank.\n\n" +
                        "• Messages are processed locally on your device.\n" +
                        "• No SMS data is sent to any server.\n" +
                        "• Transactions are only added after your review and confirmation."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSmsDisclosure = false
                    viewModel.toggleSmsDetection(true)
                    onNavigateToSmsDrafts()
                }) { Text("Enable & View Drafts") }
            },
            dismissButton = {
                TextButton(onClick = { showSmsDisclosure = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ZenTopAppBar(title = "Settings", subtitle = "Preferences") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { SettingsSectionHeader("Cloud") }
            item {
                ZenSettingsRow(
                    title = "Shared Cashbook",
                    subtitle = "Create or join a household ledger",
                    icon = Icons.Default.People,
                    onClick = onNavigateToSharedCashbook
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)) }
            item { SettingsSectionHeader("Data") }
            item {
                ZenSettingsRow(
                    title = "Backup & Restore",
                    subtitle = "Manage your data and exports",
                    icon = Icons.Default.Backup,
                    onClick = onNavigateToBackup
                )
            }
            item {
                ZenSettingsRow(
                    title = "SMS Detection",
                    subtitle = if (state.isSmsDetectionEnabled) "Active" else "Disabled",
                    icon = Icons.Default.Message,
                    onClick = {
                        if (state.isSmsDetectionEnabled) onNavigateToSmsDrafts() else showSmsDisclosure = true
                    }
                )
            }
            item {
                ZenSettingsRow(
                    title = "Voice Input",
                    subtitle = "Convert speech to transactions",
                    icon = Icons.Default.Mic,
                    onClick = { }
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)) }
            item { SettingsSectionHeader("Finance") }
            item {
                ZenSettingsRow(
                    title = "Credit Cards",
                    subtitle = "Track cards, limits, and EMIs",
                    icon = Icons.Default.CreditCard,
                    onClick = onNavigateToCreditCards
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)) }
            item { SettingsSectionHeader("Intelligence") }
            item {
                ZenSettingsRow(
                    title = "Ask ZenSpend",
                    subtitle = "AI-powered financial insights",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToAiAssistant
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)) }
            item { SettingsSectionHeader("Premium") }
            item {
                ZenSettingsRow(
                    title = if (isPremium.subscriptionState is com.android.zenspend.core.monetization.domain.model.SubscriptionState.Premium) {
                        "You are Premium"
                    } else {
                        "ZenSpend Premium"
                    },
                    subtitle = if (isPremium.subscriptionState is com.android.zenspend.core.monetization.domain.model.SubscriptionState.Premium) {
                        "Thank you for your support"
                    } else {
                        "Unlock shared cashbook & more"
                    },
                    icon = Icons.Default.WorkspacePremium,
                    onClick = onNavigateToPremium
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.ScreenPadding)) }
            item { SettingsSectionHeader("About") }
            item {
                ZenSettingsRow(
                    title = "About ZenSpend",
                    subtitle = "Version 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
                )
            }
            item {
                Text(
                    text = "Made for calm, confident money management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.SpacingXLarge)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = Dimensions.ScreenPadding,
            vertical = Dimensions.SpacingMedium
        )
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    com.android.zenspend.core.ui.theme.ZenSpendTheme {
        LazyColumn {
            item { SettingsSectionHeader("Account") }
            item {
                ZenSettingsRow(
                    title = "Shared Cashbook",
                    subtitle = "Manage household finances",
                    icon = Icons.Default.Group,
                    onClick = {}
                )
            }
        }
    }
}
