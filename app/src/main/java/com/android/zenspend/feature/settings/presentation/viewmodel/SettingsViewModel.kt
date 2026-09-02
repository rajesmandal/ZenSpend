package com.android.zenspend.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.datastore.AiPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSmsDetectionEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val aiPreferences: AiPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        aiPreferences.isSmsDetectionEnabled
            .onEach { enabled -> _uiState.update { it.copy(isSmsDetectionEnabled = enabled) } }
            .launchIn(viewModelScope)
    }

    fun toggleSmsDetection(enabled: Boolean) {
        viewModelScope.launch {
            aiPreferences.setSmsDetectionEnabled(enabled)
        }
    }
}
