package com.android.zenspend.feature.backup.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.backup.domain.usecase.*
import com.android.zenspend.feature.backup.presentation.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val application: Application,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val getBackupPreviewUseCase: GetBackupPreviewUseCase,
    private val exportTransactionsCsvUseCase: ExportTransactionsCsvUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val _effect = Channel<BackupEffect>()
    val effect = _effect.receiveAsFlow()

    private var pendingRestoreUri: Uri? = null

    private val appVersion: String
        get() = try {
            val pInfo = application.packageManager.getPackageInfo(application.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.CreateBackupRequested -> {
                val timestamp = System.currentTimeMillis()
                viewModelScope.launch {
                    _effect.send(BackupEffect.LaunchCreateBackup("zenspend_backup_$timestamp.zenspend"))
                }
            }
            BackupEvent.RestoreBackupRequested -> {
                viewModelScope.launch {
                    _effect.send(BackupEffect.LaunchRestoreFilePicker)
                }
            }
            BackupEvent.ExportCsvRequested -> {
                val timestamp = System.currentTimeMillis()
                viewModelScope.launch {
                    _effect.send(BackupEffect.LaunchCreateCsv("zenspend_transactions_$timestamp.csv"))
                }
            }
            is BackupEvent.OnBackupFileCreated -> createBackup(event.uri)
            is BackupEvent.OnRestoreFileSelected -> prepareRestore(event.uri)
            is BackupEvent.OnSafetyBackupFileCreated -> performRestore(event.uri)
            is BackupEvent.OnCsvFileCreated -> exportCsv(event.uri)
            BackupEvent.OnRestoreConfirmed -> {
                val timestamp = System.currentTimeMillis()
                viewModelScope.launch {
                    _effect.send(BackupEffect.LaunchCreateSafetyBackup("zenspend_safety_backup_$timestamp.zenspend"))
                }
            }
            BackupEvent.OnRestoreCancelled -> {
                _state.update { it.copy(isRestorePreviewVisible = false, restorePreview = null) }
                pendingRestoreUri = null
            }
            BackupEvent.OnDismissMessage -> {
                _state.update { it.copy(successMessage = null, error = null) }
            }
        }
    }

    private fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val outputStream: OutputStream? = application.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    createBackupUseCase(outputStream, appVersion)
                    _state.update { it.copy(isLoading = false, successMessage = "Backup created successfully!") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Could not open file for writing.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Backup failed: ${e.message}") }
            }
        }
    }

    private fun prepareRestore(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val inputStream: InputStream? = application.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val preview = getBackupPreviewUseCase(inputStream)
                    if (preview != null) {
                        pendingRestoreUri = uri
                        _state.update { it.copy(isLoading = false, restorePreview = preview, isRestorePreviewVisible = true) }
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Invalid backup file.") }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Could not open file for reading.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to read backup: ${e.message}") }
            }
        }
    }

    private fun performRestore(safetyUri: Uri) {
        val restoreUri = pendingRestoreUri ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isRestorePreviewVisible = false) }
            try {
                val restoreStream = application.contentResolver.openInputStream(restoreUri)
                val safetyStream = application.contentResolver.openOutputStream(safetyUri)
                
                if (restoreStream != null && safetyStream != null) {
                    val result = restoreBackupUseCase(restoreStream, safetyStream, appVersion)
                    when (result) {
                        RestoreBackupUseCase.RestoreResult.Success -> {
                            _state.update { it.copy(isLoading = false, successMessage = "Restore completed successfully!") }
                        }
                        is RestoreBackupUseCase.RestoreResult.Error -> {
                            _state.update { it.copy(isLoading = false, error = result.message) }
                        }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Could not open streams for restore.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Restore failed: ${e.message}") }
            } finally {
                pendingRestoreUri = null
            }
        }
    }

    private fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val outputStream = application.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    exportTransactionsCsvUseCase(outputStream)
                    _state.update { it.copy(isLoading = false, successMessage = "Transactions exported successfully!") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Could not open file for writing.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Export failed: ${e.message}") }
            }
        }
    }
}
