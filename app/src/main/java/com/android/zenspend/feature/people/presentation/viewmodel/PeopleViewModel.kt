package com.android.zenspend.feature.people.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.people.domain.model.Person
import com.android.zenspend.feature.people.domain.model.PersonOverview
import com.android.zenspend.feature.people.domain.repository.PeopleRepository
import com.android.zenspend.feature.people.presentation.contract.PeopleUiEvent
import com.android.zenspend.feature.people.presentation.contract.PeopleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: PeopleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observePeople(), repository.observeLoans()) { people, loans ->
                val byPerson = loans.groupBy { it.personId }
                people.map { PersonOverview(it, byPerson[it.id].orEmpty()) }
                    // Who owes you most first; settled people sink to the bottom.
                    .sortedWith(compareByDescending<PersonOverview> { it.remaining }.thenBy { it.person.name.lowercase() })
            }.collect { _uiState.value = PeopleUiState(isLoading = false, people = it) }
        }
    }

    fun handleEvent(event: PeopleUiEvent) {
        when (event) {
            is PeopleUiEvent.AddPerson -> {
                val name = event.name.trim()
                if (name.isEmpty()) return
                viewModelScope.launch { repository.addPerson(Person(name = name, phone = event.phone)) }
            }
        }
    }
}
