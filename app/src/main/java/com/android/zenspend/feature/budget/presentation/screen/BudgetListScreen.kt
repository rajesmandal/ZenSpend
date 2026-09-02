package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.budget.domain.model.BudgetProgress
import com.android.zenspend.feature.budget.presentation.contract.BudgetUiEvent
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetViewModel
import com.android.zenspend.feature.dashboard.presentation.screen.MonthSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    onNavigateToAdd: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ZenTopAppBar(title = "Budgets")
        }
    ) { padding ->
        if (state.isLoading && state.overallBudget == null && state.categoryBudgets.isEmpty()) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                item {
                    MonthSelector(
                        selectedMonth = state.selectedMonth,
                        onPrevious = { viewModel.handleEvent(BudgetUiEvent.PreviousMonth) },
                        onNext = { viewModel.handleEvent(BudgetUiEvent.NextMonth) }
                    )
                }

                state.overallBudget?.let { overall ->
                    item {
                        BudgetOverviewCard(overall)
                    }
                }

                if (state.categoryBudgets.isNotEmpty()) {
                    item {
                        Text("Category Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(state.categoryBudgets) { budget ->
                        CategoryBudgetBar(budget)
                    }
                } else if (state.overallBudget == null) {
                    item {
                        EmptyState(
                            title = "No Budgets",
                            message = "Plan your spending by setting up a monthly budget.",
                            action = { ZenPrimaryButton(text = "Add Budget", onClick = onNavigateToAdd) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetOverviewCard(progress: BudgetProgress) {
    ZenSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Text("Monthly Budget", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${"%.2f".format(progress.spentAmount / 100.0)} / ₹${"%.2f".format(progress.budget.amount / 100.0)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (progress.isOverBudget) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            LinearProgressIndicator(
                progress = { progress.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress.isOverBudget) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            if (progress.isOverBudget) {
                Text(
                    "Over budget by ₹${"%.2f".format((progress.spentAmount - progress.budget.amount) / 100.0)}",
                    color = ZenSpendTheme.colors.expense,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
                )
            }
    }
}

@Composable
fun CategoryBudgetBar(progress: BudgetProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(progress.categoryName ?: "Unknown", style = MaterialTheme.typography.bodyLarge)
            Text(
                "₹${"%.2f".format(progress.spentAmount / 100.0)} of ₹${"%.2f".format(progress.budget.amount / 100.0)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingXSmall))
        LinearProgressIndicator(
            progress = { progress.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = if (progress.isOverBudget) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
