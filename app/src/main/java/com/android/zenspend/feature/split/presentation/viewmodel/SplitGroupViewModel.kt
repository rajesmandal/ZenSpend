package com.android.zenspend.feature.split.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import com.android.zenspend.feature.split.domain.repository.SplitRepository
import com.android.zenspend.feature.split.presentation.contract.SplitGroupUiEffect
import com.android.zenspend.feature.split.presentation.contract.SplitGroupUiEvent
import com.android.zenspend.feature.split.presentation.contract.SplitGroupUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitGroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SplitRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(SplitGroupUiState())
    val uiState: StateFlow<SplitGroupUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SplitGroupUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SplitGroupUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeGroup(groupId).collect { _uiState.value = SplitGroupUiState(isLoading = false, details = it) }
        }
    }

    fun handleEvent(event: SplitGroupUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SplitGroupUiEvent.AddMember -> repository.addMember(groupId, event.name)
                is SplitGroupUiEvent.RemoveMember ->
                    if (!repository.removeMember(event.memberId)) {
                        _uiEffect.send(SplitGroupUiEffect.ShowMessage(R.string.split_member_in_use))
                    }
                is SplitGroupUiEvent.SettleUp -> {
                    val ok = repository.addSettlement(
                        SplitSettlement(
                            groupId = groupId, from = event.from, to = event.to,
                            amount = event.amount, date = System.currentTimeMillis()
                        )
                    )
                    if (ok) _uiEffect.send(SplitGroupUiEffect.ShowMessage(R.string.split_message_settled))
                }
                is SplitGroupUiEvent.DeleteSettlement -> repository.deleteSettlement(event.id)
                is SplitGroupUiEvent.DeleteExpense -> repository.deleteExpense(event.id)
                SplitGroupUiEvent.DeleteGroup -> {
                    repository.deleteGroup(groupId)
                    _uiEffect.send(SplitGroupUiEffect.NavigateBack)
                }
            }
        }
    }
}
