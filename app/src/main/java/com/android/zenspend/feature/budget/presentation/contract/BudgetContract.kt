package com.android.zenspend.feature.budget.presentation.contract

import com.android.zenspend.feature.budget.domain.model.BudgetProgress
import java.util.Calendar

data class BudgetUiState(
    val isLoading: Boolean = false,
    val selectedMonth: Calendar = Calendar.getInstance(),
    val overallBudget: BudgetProgress? = null,
    val categoryBudgets: List<BudgetProgress> = emptyList(),
    val error: String? = null,
    
    // Form state
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val isSubmitting: Boolean = false
)

sealed interface BudgetUiEvent {
    object LoadBudgets : BudgetUiEvent
    object NextMonth : BudgetUiEvent
    object PreviousMonth : BudgetUiEvent
    
    // Form events
    data class OnAmountChange(val amount: String) : BudgetUiEvent
    data class OnCategoryChange(val categoryId: Long?) : BudgetUiEvent
    object SaveBudget : BudgetUiEvent
    
    data class DeleteBudget(val id: Long) : BudgetUiEvent
}

sealed interface BudgetUiEffect {
    data class ShowSnackbar(val message: String) : BudgetUiEffect
    object NavigateBack : BudgetUiEffect
}
