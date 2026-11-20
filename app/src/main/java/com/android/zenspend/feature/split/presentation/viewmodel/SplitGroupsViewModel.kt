package com.android.zenspend.feature.split.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.split.domain.repository.SplitRepository
import com.android.zenspend.feature.split.presentation.contract.SplitGroupsUiEvent
import com.android.zenspend.feature.split.presentation.contract.SplitGroupsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitGroupsViewModel @Inject constructor(
    private val repository: SplitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitGroupsUiState())
    val uiState: StateFlow<SplitGroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeGroups().collect { _uiState.value = SplitGroupsUiState(isLoading = false, groups = it) }
        }
    }

    fun handleEvent(event: SplitGroupsUiEvent) {
        when (event) {
            is SplitGroupsUiEvent.CreateGroup -> {
                val name = event.name.trim()
                if (name.isEmpty()) return
                viewModelScope.launch { repository.createGroup(name, event.myName, event.others) }
            }
        }
    }
}
