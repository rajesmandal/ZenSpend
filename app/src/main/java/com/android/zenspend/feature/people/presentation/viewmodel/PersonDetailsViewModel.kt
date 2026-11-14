package com.android.zenspend.feature.people.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.people.domain.model.PersonOverview
import com.android.zenspend.feature.people.domain.repository.PeopleRepository
import com.android.zenspend.feature.people.presentation.contract.PersonDetailsUiEffect
import com.android.zenspend.feature.people.presentation.contract.PersonDetailsUiEvent
import com.android.zenspend.feature.people.presentation.contract.PersonDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PeopleRepository
) : ViewModel() {

    private val personId: Long = savedStateHandle.get<String>("personId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(PersonDetailsUiState())
    val uiState: StateFlow<PersonDetailsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PersonDetailsUiEffect>()
    val uiEffect: Flow<PersonDetailsUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(repository.observePerson(personId), repository.observeLoansOf(personId)) { person, loans ->
                PersonDetailsUiState(isLoading = false, overview = person?.let { PersonOverview(it, loans) })
            }.collect { _uiState.value = it }
        }
    }

    fun handleEvent(event: PersonDetailsUiEvent) {
        when (event) {
            PersonDetailsUiEvent.Delete -> viewModelScope.launch {
                repository.deletePerson(personId)
                _uiEffect.send(PersonDetailsUiEffect.NavigateBack)
            }
        }
    }
}
