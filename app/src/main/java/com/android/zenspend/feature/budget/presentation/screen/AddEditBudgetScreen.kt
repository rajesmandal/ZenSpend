package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.budget.presentation.contract.BudgetUiEffect
import com.android.zenspend.feature.budget.presentation.contract.BudgetUiEvent
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetViewModel
import com.android.zenspend.feature.transaction.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    onNavigateBack: () -> Unit,
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val budgetState by budgetViewModel.uiState.collectAsStateWithLifecycle()
    val txState by transactionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        budgetViewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is BudgetUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is BudgetUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Budget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
        ) {
            Text("Budget Amount", style = MaterialTheme.typography.labelMedium)
            AmountInput(
                value = budgetState.amountText,
                onValueChange = { budgetViewModel.handleEvent(BudgetUiEvent.OnAmountChange(it)) }
            )

            com.android.zenspend.feature.transaction.presentation.screen.CategorySelector(
                selectedId = budgetState.selectedCategoryId,
                categories = txState.categories,
                onCategorySelected = { budgetViewModel.handleEvent(BudgetUiEvent.OnCategoryChange(it)) }
            )
            
            Text("Tip: Leave category empty for overall monthly budget.", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.weight(1f))

            ZenPrimaryButton(
                text = "Save Budget",
                onClick = { budgetViewModel.handleEvent(BudgetUiEvent.SaveBudget) },
                loading = budgetState.isSubmitting
            )
        }
    }
}
