package com.android.zenspend.feature.report.presentation.contract

import com.android.zenspend.feature.report.domain.model.*
import java.util.Calendar

data class ReportUiState(
    val isLoading: Boolean = false,
    val selectedMonth: Calendar = Calendar.getInstance(),
    val overview: FinancialOverview? = null,
    val expenseCategories: List<CategoryReport> = emptyList(),
    val incomeCategories: List<CategoryReport> = emptyList(),
    val trendPoints: List<TrendPoint> = emptyList(),
    val walletActivity: List<WalletActivityReport> = emptyList(),
    val creditCards: List<CreditCardReport> = emptyList(),
    val budgetReports: List<com.android.zenspend.feature.budget.domain.model.BudgetProgress> = emptyList(),
    val emiReport: EMIReport? = null,
    val error: String? = null
)

sealed interface ReportUiEvent {
    object LoadReports : ReportUiEvent
    object NextMonth : ReportUiEvent
    object PreviousMonth : ReportUiEvent
    object Refresh : ReportUiEvent
}

sealed interface ReportUiEffect {
    data class ShowSnackbar(val message: String) : ReportUiEffect
}
