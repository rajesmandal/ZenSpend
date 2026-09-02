package com.android.zenspend.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.android.zenspend.feature.dashboard.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase,
    val adController: com.android.zenspend.core.monetization.presentation.AdController
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DashboardUiEffect>()
    val uiEffect: Flow<DashboardUiEffect> = _uiEffect.receiveAsFlow()

    private var summaryJob: Job? = null

    init {
        handleEvent(DashboardUiEvent.LoadSummary)
    }

    fun handleEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.LoadSummary -> observeSummary()
            is DashboardUiEvent.NextMonth -> changeMonth(1)
            is DashboardUiEvent.PreviousMonth -> changeMonth(-1)
            is DashboardUiEvent.Refresh -> observeSummary()
        }
    }

    private fun observeSummary() {
        summaryJob?.cancel()
        
        val calendar = _uiState.value.selectedMonth
        val (start, end) = getMonthRange(calendar)

        summaryJob = observeDashboardSummaryUseCase(start, end)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { summary ->
                _uiState.update { it.copy(isLoading = false, summary = summary, error = null) }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun changeMonth(delta: Int) {
        _uiState.update { state ->
            val newCalendar = state.selectedMonth.clone() as Calendar
            newCalendar.add(Calendar.MONTH, delta)
            state.copy(selectedMonth = newCalendar)
        }
        observeSummary()
    }

    private fun getMonthRange(calendar: Calendar): Pair<Long, Long> {
        val start = calendar.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val end = start.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        end.add(Calendar.MILLISECOND, -1)

        return start.timeInMillis to end.timeInMillis
    }
}
