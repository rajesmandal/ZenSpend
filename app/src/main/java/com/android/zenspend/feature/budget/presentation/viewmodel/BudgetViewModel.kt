package com.android.zenspend.feature.budget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.usecase.*
import com.android.zenspend.feature.budget.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val observeBudgetsWithProgressUseCase: ObserveBudgetsWithProgressUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val deleteBudgetUseCase: ArchiveBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<BudgetUiEffect>()
    val uiEffect: Flow<BudgetUiEffect> = _uiEffect.receiveAsFlow()

    private var budgetsJob: Job? = null

    init {
        handleEvent(BudgetUiEvent.LoadBudgets)
    }

    fun handleEvent(event: BudgetUiEvent) {
        when (event) {
            is BudgetUiEvent.LoadBudgets -> observeBudgets()
            is BudgetUiEvent.NextMonth -> changeMonth(1)
            is BudgetUiEvent.PreviousMonth -> changeMonth(-1)
            is BudgetUiEvent.OnAmountChange -> _uiState.update { it.copy(amountText = event.amount) }
            is BudgetUiEvent.OnCategoryChange -> _uiState.update { it.copy(selectedCategoryId = event.categoryId) }
            is BudgetUiEvent.SaveBudget -> saveBudget()
            is BudgetUiEvent.DeleteBudget -> {} // TODO
        }
    }

    private fun observeBudgets() {
        budgetsJob?.cancel()
        val calendar = _uiState.value.selectedMonth
        val (start, end) = getMonthRange(calendar)

        budgetsJob = observeBudgetsWithProgressUseCase(start, end)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { progressList ->
                val overall = progressList.find { it.budget.categoryId == null }
                val categories = progressList.filter { it.budget.categoryId != null }
                _uiState.update { it.copy(isLoading = false, overallBudget = overall, categoryBudgets = categories) }
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
        observeBudgets()
    }

    private fun saveBudget() {
        val amount = _uiState.value.amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        val calendar = _uiState.value.selectedMonth
        val (start, end) = getMonthRange(calendar)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = addBudgetUseCase(
                Budget(
                    amount = amount,
                    categoryId = _uiState.value.selectedCategoryId,
                    startDate = start,
                    endDate = end
                )
            )
            when (result) {
                is Resource.Success -> {
                    _uiEffect.send(BudgetUiEffect.ShowSnackbar("Budget saved"))
                    _uiEffect.send(BudgetUiEffect.NavigateBack)
                }
                is Resource.Error -> {
                    _uiEffect.send(BudgetUiEffect.ShowSnackbar("Error: ${result.error}"))
                }
                else -> {}
            }
            _uiState.update { it.copy(isSubmitting = false) }
        }
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
