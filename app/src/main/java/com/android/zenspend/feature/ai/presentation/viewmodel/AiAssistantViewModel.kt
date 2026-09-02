package com.android.zenspend.feature.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.datastore.AiPreferences
import com.android.zenspend.feature.ai.domain.model.*
import com.android.zenspend.feature.ai.domain.usecase.ChatWithAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiAssistantState(
    val messages: List<AiMessage> = emptyList(),
    val isDisclaimerAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = ""
)

sealed class AiAssistantEvent {
    data class OnQueryChanged(val query: String) : AiAssistantEvent()
    object OnSendClicked : AiAssistantEvent()
    object OnDisclaimerAccepted : AiAssistantEvent()
    data class OnSuggestedQuestionClicked(val question: String) : AiAssistantEvent()
}

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val chatWithAiUseCase: ChatWithAiUseCase,
    private val aiPreferences: AiPreferences,
    private val aiQuotaManager: com.android.zenspend.feature.ai.domain.AiQuotaManager,
    val entitlementManager: com.android.zenspend.core.monetization.domain.EntitlementManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantState())
    val uiState: StateFlow<AiAssistantState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        aiPreferences.isDisclaimerAccepted
            .onEach { accepted -> _uiState.update { it.copy(isDisclaimerAccepted = accepted) } }
            .launchIn(viewModelScope)
    }

    fun handleEvent(event: AiAssistantEvent) {
        when (event) {
            is AiAssistantEvent.OnQueryChanged -> _uiState.update { it.copy(query = event.query) }
            AiAssistantEvent.OnSendClicked -> sendMessage(_uiState.value.query)
            AiAssistantEvent.OnDisclaimerAccepted -> acceptDisclaimer()
            is AiAssistantEvent.OnSuggestedQuestionClicked -> sendMessage(event.question)
        }
    }

    private fun acceptDisclaimer() {
        viewModelScope.launch {
            aiPreferences.setDisclaimerAccepted(true)
        }
    }

    private fun sendMessage(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            if (!aiQuotaManager.canExecuteQuery()) {
                _uiState.update { it.copy(error = "Daily limit reached. Upgrade to Premium for unlimited access!") }
                return@launch
            }

            val userMessage = AiMessage(UUID.randomUUID().toString(), query, AiRole.USER)
            _uiState.update { it.copy(
                messages = it.messages + userMessage,
                query = "",
                isLoading = true,
                error = null
            ) }

            chatWithAiUseCase(query)
                .onEach { response ->
                    when (response) {
                        is AiResponse.Success -> {
                            aiQuotaManager.recordQuery()
                            val remaining = aiQuotaManager.getRemainingQuota()
                            val assistantMessage = AiMessage(
                                id = UUID.randomUUID().toString(),
                                content = response.content + if (remaining < 5) "\n\n(Daily quota remaining: $remaining)" else "",
                                role = AiRole.ASSISTANT
                            )
                            _uiState.update { it.copy(
                                messages = it.messages + assistantMessage,
                                isLoading = false
                            ) }
                        }
                        is AiResponse.Error -> {
                            _uiState.update { it.copy(
                                error = response.message,
                                isLoading = false
                            ) }
                        }
                        AiResponse.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .launchIn(viewModelScope)
        }
    }
}
