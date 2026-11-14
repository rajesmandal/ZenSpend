package com.android.zenspend.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.datastore.AiPreferences
import com.android.zenspend.core.datastore.QuickAddPreferences
import com.android.zenspend.core.quickadd.QuickAddNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSmsDetectionEnabled: Boolean = false,
    val isQuickAddNotificationEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val aiPreferences: AiPreferences,
    private val quickAddPreferences: QuickAddPreferences,
    private val quickAddNotifier: QuickAddNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        aiPreferences.isSmsDetectionEnabled
            .onEach { enabled -> _uiState.update { it.copy(isSmsDetectionEnabled = enabled) } }
            .launchIn(viewModelScope)
        quickAddPreferences.isNotificationEnabled
            .onEach { enabled -> _uiState.update { it.copy(isQuickAddNotificationEnabled = enabled) } }
            .launchIn(viewModelScope)
    }

    fun setQuickAddNotification(enabled: Boolean) {
        viewModelScope.launch {
            quickAddPreferences.setNotificationEnabled(enabled)
            quickAddNotifier.sync()
        }
    }

    fun toggleSmsDetection(enabled: Boolean) {
        viewModelScope.launch {
            aiPreferences.setSmsDetectionEnabled(enabled)
        }
    }
}
