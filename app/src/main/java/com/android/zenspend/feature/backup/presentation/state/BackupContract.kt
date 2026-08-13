package com.android.zenspend.feature.backup.presentation.state

import com.android.zenspend.feature.backup.domain.usecase.GetBackupPreviewUseCase

data class BackupState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val restorePreview: GetBackupPreviewUseCase.BackupPreview? = null,
    val isRestorePreviewVisible: Boolean = false
)

sealed class BackupEvent {
    object CreateBackupRequested : BackupEvent()
    object RestoreBackupRequested : BackupEvent()
    object ExportCsvRequested : BackupEvent()
    
    data class OnBackupFileCreated(val uri: android.net.Uri) : BackupEvent()
    data class OnRestoreFileSelected(val uri: android.net.Uri) : BackupEvent()
    data class OnSafetyBackupFileCreated(val uri: android.net.Uri) : BackupEvent()
    data class OnCsvFileCreated(val uri: android.net.Uri) : BackupEvent()
    
    object OnRestoreConfirmed : BackupEvent()
    object OnRestoreCancelled : BackupEvent()
    object OnDismissMessage : BackupEvent()
}

sealed class BackupEffect {
    data class LaunchCreateBackup(val fileName: String) : BackupEffect()
    object LaunchRestoreFilePicker : BackupEffect()
    data class LaunchCreateSafetyBackup(val fileName: String) : BackupEffect()
    data class LaunchCreateCsv(val fileName: String) : BackupEffect()
}
