package com.android.zenspend.feature.backup.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenSurfaceCard
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.backup.presentation.state.BackupEffect
import com.android.zenspend.feature.backup.presentation.state.BackupEvent
import com.android.zenspend.feature.backup.presentation.viewmodel.BackupViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri -> uri?.let { viewModel.onEvent(BackupEvent.OnBackupFileCreated(it)) } }
    )

    val restorePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.onEvent(BackupEvent.OnRestoreFileSelected(it)) } }
    )

    val safetyBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri -> uri?.let { viewModel.onEvent(BackupEvent.OnSafetyBackupFileCreated(it)) } }
    )

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri -> uri?.let { viewModel.onEvent(BackupEvent.OnCsvFileCreated(it)) } }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BackupEffect.LaunchCreateBackup -> createBackupLauncher.launch(effect.fileName)
                is BackupEffect.LaunchRestoreFilePicker -> restorePickerLauncher.launch(arrayOf("*/*"))
                is BackupEffect.LaunchCreateSafetyBackup -> safetyBackupLauncher.launch(effect.fileName)
                is BackupEffect.LaunchCreateCsv -> exportCsvLauncher.launch(effect.fileName)
            }
        }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(BackupEvent.OnDismissMessage)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(BackupEvent.OnDismissMessage)
        }
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Backup & Data",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
        ) {
            item {
                BackupActionCard(
                    title = "Create Backup",
                    description = "Export all your data to a .zenspend file.",
                    icon = Icons.Default.Backup,
                    onClick = { viewModel.onEvent(BackupEvent.CreateBackupRequested) }
                )
            }
            item {
                BackupActionCard(
                    title = "Restore Backup",
                    description = "Restore data from a .zenspend file. This will replace current data.",
                    icon = Icons.Default.Restore,
                    onClick = { viewModel.onEvent(BackupEvent.RestoreBackupRequested) }
                )
            }
            item {
                BackupActionCard(
                    title = "Export Transactions",
                    description = "Download your transaction history as a CSV file.",
                    icon = Icons.Default.FileUpload,
                    onClick = { viewModel.onEvent(BackupEvent.ExportCsvRequested) }
                )
            }
        }

        if (state.isRestorePreviewVisible && state.restorePreview != null) {
            RestorePreviewDialog(
                preview = state.restorePreview!!,
                onConfirm = { viewModel.onEvent(BackupEvent.OnRestoreConfirmed) },
                onDismiss = { viewModel.onEvent(BackupEvent.OnRestoreCancelled) }
            )
        }
    }
}

@Composable
private fun BackupActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ZenSurfaceCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Dimensions.SpacingLarge))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
