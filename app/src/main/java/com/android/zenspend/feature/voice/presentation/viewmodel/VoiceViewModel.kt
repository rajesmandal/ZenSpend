package com.android.zenspend.feature.voice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.voice.data.manager.VoiceInputManager
import com.android.zenspend.feature.voice.data.manager.VoiceInputState
import com.android.zenspend.feature.voice.data.parser.VoiceTransactionParser
import com.android.zenspend.feature.voice.domain.model.ParsingStatus
import com.android.zenspend.feature.voice.domain.model.VoiceField
import com.android.zenspend.feature.voice.domain.model.VoiceTransactionDraft
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val inputState: VoiceInputState = VoiceInputState.Idle,
    val draft: VoiceTransactionDraft? = null,
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class VoiceUiEvent {
    object StartListening : VoiceUiEvent()
    object StopListening : VoiceUiEvent()
    object Cancel : VoiceUiEvent()
    object Reset : VoiceUiEvent()
}

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceInputManager: VoiceInputManager,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val getCategorySuggestionUseCase: com.android.zenspend.feature.category.domain.usecase.GetCategorySuggestionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var isConfirmationInProgress = false

    fun handleEvent(event: VoiceUiEvent) {
        when (event) {
            VoiceUiEvent.StartListening -> startListening()
            VoiceUiEvent.StopListening -> { /* Managed by VoiceInputManager cleanup */ }
            VoiceUiEvent.Cancel -> {
                _uiState.update { VoiceUiState() }
            }
            VoiceUiEvent.Reset -> {
                _uiState.update { it.copy(draft = null, inputState = VoiceInputState.Idle, error = null) }
            }
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            voiceInputManager.startListening()
                .onEach { inputState ->
                    _uiState.update { it.copy(inputState = inputState) }
                    if (inputState is VoiceInputState.Success) {
                        parseTranscript(inputState.transcript)
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .launchIn(viewModelScope)
        }
    }

    private suspend fun parseTranscript(transcript: String) {
        _uiState.update { it.copy(isProcessing = true) }
        try {
            val wallets = walletRepository.getAllActiveWallets().first()
            val categories = categoryRepository.getAllActiveCategories().first()
            
            var draft = VoiceTransactionParser.parse(transcript, wallets, categories)
            
            if (draft.categoryId == null && draft.type != null) {
                val suggestion = getCategorySuggestionUseCase(
                    merchant = draft.merchant,
                    title = draft.merchant,
                    type = draft.type
                )
                if (suggestion != null) {
                    draft = draft.copy(
                        categoryId = suggestion.categoryId,
                        parsingStatus = if (draft.parsingStatus == ParsingStatus.INCOMPLETE && !draft.missingFields.contains(VoiceField.AMOUNT)) 
                            ParsingStatus.SUCCESS else draft.parsingStatus
                    )
                }
            }
            
            _uiState.update { it.copy(draft = draft, isProcessing = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Parsing failed: ${e.message}", isProcessing = false) }
        }
    }
    
    // Idempotency protection for confirmation is handled in the UI/ViewModel of AddTransaction 
    // when populating from this draft.
}
