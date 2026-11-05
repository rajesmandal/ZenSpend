package com.android.zenspend.feature.dashboard.presentation.contract

import com.android.zenspend.feature.dashboard.domain.model.DashboardSummary
import java.util.Calendar

data class DashboardUiState(
    val isLoading: Boolean = false,
    val summary: DashboardSummary? = null,
    val selectedMonth: Calendar = Calendar.getInstance(),
    val error: String? = null
)

sealed interface DashboardUiEvent {
    object LoadSummary : DashboardUiEvent
    data class SelectMonth(val month: java.util.Calendar) : DashboardUiEvent
    object Refresh : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data class ShowSnackbar(val message: String) : DashboardUiEffect
    object NavigateToTransactions : DashboardUiEffect
    object NavigateToWallets : DashboardUiEffect
}
