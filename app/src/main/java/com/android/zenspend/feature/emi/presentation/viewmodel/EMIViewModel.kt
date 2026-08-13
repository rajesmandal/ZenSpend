package com.android.zenspend.feature.emi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.emi.domain.usecase.ObserveEMIsUseCase
import com.android.zenspend.feature.emi.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class EMIViewModel @Inject constructor(
    private val observeEMIsUseCase: ObserveEMIsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EMIUiState())
    val uiState: StateFlow<EMIUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EMIUiEffect>()
    val uiEffect: Flow<EMIUiEffect> = _uiEffect.receiveAsFlow()

    init {
        handleEvent(EMIUiEvent.LoadEMIs)
    }

    fun handleEvent(event: EMIUiEvent) {
        when (event) {
            is EMIUiEvent.LoadEMIs -> observeEMIs()
        }
    }

    private fun observeEMIs() {
        observeEMIsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { list ->
                _uiState.update { it.copy(isLoading = false, emis = list) }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }
}
