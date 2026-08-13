package com.android.zenspend.feature.sms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.usecase.GetSmsDraftsUseCase
import com.android.zenspend.feature.sms.domain.usecase.IgnoreSmsDraftUseCase
import com.android.zenspend.feature.sms.domain.usecase.SyncHistoricalSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsDraftListState(
    val drafts: List<SmsTransactionDraft> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SmsDraftListEvent {
    object SyncHistoricalRequested : SmsDraftListEvent()
    data class IgnoreRequested(val id: Long) : SmsDraftListEvent()
    data class ConfirmRequested(val draft: SmsTransactionDraft) : SmsDraftListEvent()
}

@HiltViewModel
class SmsDraftListViewModel @Inject constructor(
    private val getSmsDraftsUseCase: GetSmsDraftsUseCase,
    private val ignoreSmsDraftUseCase: IgnoreSmsDraftUseCase,
    private val syncHistoricalSmsUseCase: SyncHistoricalSmsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SmsDraftListState())
    val state: StateFlow<SmsDraftListState> = _state.asStateFlow()

    init {
        loadDrafts()
    }

    private fun loadDrafts() {
        getSmsDraftsUseCase()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { drafts -> _state.update { it.copy(drafts = drafts, isLoading = false) } }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SmsDraftListEvent) {
        when (event) {
            SmsDraftListEvent.SyncHistoricalRequested -> syncHistorical()
            is SmsDraftListEvent.IgnoreRequested -> ignoreDraft(event.id)
            is SmsDraftListEvent.ConfirmRequested -> { /* Handled by navigation to review screen */ }
        }
    }

    private fun syncHistorical() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                syncHistoricalSmsUseCase()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun ignoreDraft(id: Long) {
        viewModelScope.launch {
            ignoreSmsDraftUseCase(id)
        }
    }
}
