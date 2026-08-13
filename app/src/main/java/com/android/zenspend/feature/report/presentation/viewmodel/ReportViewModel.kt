package com.android.zenspend.feature.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.report.domain.usecase.*
import com.android.zenspend.feature.report.presentation.contract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase,
    private val getExpenseCategoryAnalysisUseCase: GetExpenseCategoryAnalysisUseCase,
    private val getIncomeCategoryAnalysisUseCase: GetIncomeCategoryAnalysisUseCase,
    private val getTrendAnalysisUseCase: GetTrendAnalysisUseCase,
    private val getWalletAnalysisUseCase: GetWalletAnalysisUseCase,
    private val getCreditCardAnalysisUseCase: GetCreditCardAnalysisUseCase,
    private val getEmiSummaryUseCase: GetEmiSummaryUseCase,
    private val observeBudgetsWithProgressUseCase: com.android.zenspend.feature.budget.domain.usecase.ObserveBudgetsWithProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ReportUiEffect>()
    val uiEffect: Flow<ReportUiEffect> = _uiEffect.receiveAsFlow()

    private var reportJob: Job? = null

    init {
        handleEvent(ReportUiEvent.LoadReports)
    }

    fun handleEvent(event: ReportUiEvent) {
        when (event) {
            is ReportUiEvent.LoadReports -> observeReports()
            is ReportUiEvent.NextMonth -> changeMonth(1)
            is ReportUiEvent.PreviousMonth -> changeMonth(-1)
            is ReportUiEvent.Refresh -> observeReports()
        }
    }

    private fun observeReports() {
        reportJob?.cancel()
        val calendar = _uiState.value.selectedMonth
        val (start, end) = getMonthRange(calendar)
        val (prevStart, prevEnd) = getPreviousMonthRange(calendar)

        reportJob = combine(
            getFinancialOverviewUseCase(start, end, prevStart, prevEnd),
            getExpenseCategoryAnalysisUseCase(start, end),
            getIncomeCategoryAnalysisUseCase(start, end),
            getTrendAnalysisUseCase(start, end),
            getWalletAnalysisUseCase(start, end),
            getCreditCardAnalysisUseCase(start, end),
            getEmiSummaryUseCase(),
            observeBudgetsWithProgressUseCase(start, end)
        ) { args ->
            val overview = args[0] as com.android.zenspend.feature.report.domain.model.FinancialOverview
            val expenses = args[1] as List<com.android.zenspend.feature.report.domain.model.CategoryReport>
            val incomeCat = args[2] as List<com.android.zenspend.feature.report.domain.model.CategoryReport>
            val trend = args[3] as List<com.android.zenspend.feature.report.domain.model.TrendPoint>
            val wallets = args[4] as List<com.android.zenspend.feature.report.domain.model.WalletActivityReport>
            val cards = args[5] as List<com.android.zenspend.feature.report.domain.model.CreditCardReport>
            val emi = args[6] as com.android.zenspend.feature.report.domain.model.EMIReport
            val budgets = args[7] as List<com.android.zenspend.feature.budget.domain.model.BudgetProgress>

            _uiState.update { it.copy(
                isLoading = false,
                overview = overview,
                expenseCategories = expenses,
                incomeCategories = incomeCat,
                trendPoints = trend,
                walletActivity = wallets,
                creditCards = cards,
                emiReport = emi,
                budgetReports = budgets,
                error = null
            ) }
        }.onStart { _uiState.update { it.copy(isLoading = true) } }
        .catch { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
        .launchIn(viewModelScope)
    }

    private fun changeMonth(delta: Int) {
        _uiState.update { state ->
            val newCalendar = state.selectedMonth.clone() as Calendar
            newCalendar.add(Calendar.MONTH, delta)
            state.copy(selectedMonth = newCalendar)
        }
        observeReports()
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

    private fun getPreviousMonthRange(calendar: Calendar): Pair<Long, Long> {
        val prev = calendar.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        return getMonthRange(prev)
    }
}
