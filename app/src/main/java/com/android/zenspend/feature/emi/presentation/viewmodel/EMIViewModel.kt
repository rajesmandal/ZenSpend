package com.android.zenspend.feature.emi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.emi.domain.usecase.ObserveEMIsUseCase
import com.android.zenspend.feature.emi.presentation.contract.EMIUiEffect
import com.android.zenspend.feature.emi.presentation.contract.EMIUiEvent
import com.android.zenspend.feature.emi.presentation.contract.EMIUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
            EMIUiEvent.LoadEMIs -> observeEMIs()
            is EMIUiEvent.SelectFilter -> _uiState.update { it.copy(filter = event.filter) }
            is EMIUiEvent.ShowClosed -> _uiState.update { it.copy(showClosed = event.closed) }
        }
    }

    private fun observeEMIs() {
        observeEMIsUseCase()
            .onEach { list -> _uiState.update { it.copy(isLoading = false, allEmis = list, error = null) } }
            .catch { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            .launchIn(viewModelScope)
    }
}
