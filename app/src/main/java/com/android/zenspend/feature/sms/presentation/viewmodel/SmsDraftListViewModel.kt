package com.android.zenspend.feature.sms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.datastore.CardSuggestionPreferences
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.creditcard.domain.suggestion.CardSuggestion
import com.android.zenspend.feature.creditcard.domain.suggestion.CardSuggestions
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
    /** Cards seen in the SMS that are not saved in the app yet. */
    val cardSuggestions: List<CardSuggestion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SmsDraftListEvent {
    object SyncHistoricalRequested : SmsDraftListEvent()
    /** SMS permission is granted: make sure detection is on and import existing messages. */
    object AccessGranted : SmsDraftListEvent()
    data class IgnoreRequested(val id: Long) : SmsDraftListEvent()
    data class DismissCardSuggestion(val last4: String) : SmsDraftListEvent()
    data class ConfirmRequested(val draft: SmsTransactionDraft) : SmsDraftListEvent()
}

@HiltViewModel
class SmsDraftListViewModel @Inject constructor(
    private val getSmsDraftsUseCase: GetSmsDraftsUseCase,
    private val ignoreSmsDraftUseCase: IgnoreSmsDraftUseCase,
    private val syncHistoricalSmsUseCase: SyncHistoricalSmsUseCase,
    private val aiPreferences: com.android.zenspend.core.datastore.AiPreferences,
    private val creditCardRepository: CreditCardRepository,
    private val cardSuggestionPreferences: CardSuggestionPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SmsDraftListState())
    val state: StateFlow<SmsDraftListState> = _state.asStateFlow()

    init {
        loadDrafts()
    }

    private fun loadDrafts() {
        combine(
            getSmsDraftsUseCase(),
            creditCardRepository.getAllActiveCreditCards(),
            cardSuggestionPreferences.dismissed
        ) { drafts, cards, dismissed -> drafts to CardSuggestions.from(drafts, cards, dismissed) }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { (drafts, suggestions) ->
                _state.update { it.copy(drafts = drafts, cardSuggestions = suggestions, isLoading = false) }
            }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SmsDraftListEvent) {
        when (event) {
            SmsDraftListEvent.SyncHistoricalRequested -> syncHistorical()
            SmsDraftListEvent.AccessGranted -> viewModelScope.launch {
                aiPreferences.setSmsDetectionEnabled(true)
                syncHistorical()
            }
            is SmsDraftListEvent.IgnoreRequested -> ignoreDraft(event.id)
            is SmsDraftListEvent.DismissCardSuggestion ->
                viewModelScope.launch { cardSuggestionPreferences.dismiss(event.last4) }
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
