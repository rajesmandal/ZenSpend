package com.android.zenspend.feature.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBackup: () -> Unit,
    onNavigateToSmsDrafts: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    var showSmsDisclosure by remember { mutableStateOf(false) }

    if (showSmsDisclosure) {
        AlertDialog(
            onDismissRequest = { showSmsDisclosure = false },
            title = { Text("SMS Transaction Detection") },
            text = {
                Text("ZenSpend can help you track expenses by detecting transaction SMS messages from your bank. \n\n" +
                     "• Messages are processed locally on your device.\n" +
                     "• No SMS data is sent to any server.\n" +
                     "• Transactions are only added after your review and confirmation.")
            },
            confirmButton = {
                TextButton(onClick = { 
                    showSmsDisclosure = false
                    onNavigateToSmsDrafts()
                }) {
                    Text("Enable & View Drafts")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsDisclosure = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsSectionHeader("Data Management")
            }
            item {
                SettingsItem(
                    title = "Backup & Restore",
                    subtitle = "Manage your data and exports",
                    icon = Icons.Default.Backup,
                    onClick = onNavigateToBackup
                )
            }
            item {
                SettingsItem(
                    title = "SMS Detection",
                    subtitle = "Manage detected transaction drafts",
                    icon = Icons.Default.Message,
                    onClick = { showSmsDisclosure = true }
                )
            }
            item {
                SettingsItem(
                    title = "Voice Input",
                    subtitle = "Convert speech to transactions",
                    icon = Icons.Default.Mic,
                    onClick = { /* Status is shown on use */ }
                )
            }
            item {
                SettingsSectionHeader("AI Assistant")
            }
            item {
                SettingsItem(
                    title = "Ask ZenSpend",
                    subtitle = "Get financial insights using AI",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToAiAssistant
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsSectionHeader("Premium")
            }
            item {
                SettingsItem(
                    title = "ZenSpend Premium",
                    subtitle = "Unlock advanced features",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToPremium
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsSectionHeader("About")
            }
            item {
                SettingsItem(
                    title = "About ZenSpend",
                    subtitle = "Version 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
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
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
