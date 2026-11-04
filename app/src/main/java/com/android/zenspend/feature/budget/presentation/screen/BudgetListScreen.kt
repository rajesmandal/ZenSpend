package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenRingProgress
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.budget.domain.model.BudgetProgress
import com.android.zenspend.feature.budget.presentation.contract.BudgetUiEvent
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetViewModel
import com.android.zenspend.feature.dashboard.presentation.screen.MonthSelector
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    onNavigateToAdd: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.budgets_title),
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.budgets_add))
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.overallBudget == null && state.categoryBudgets.isEmpty()) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.ScreenPadding,
                    vertical = Dimensions.SpacingLarge
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                item {
                    MonthSelector(
                        selectedMonth = state.selectedMonth,
                        onPrevious = { viewModel.handleEvent(BudgetUiEvent.PreviousMonth) },
                        onNext = { viewModel.handleEvent(BudgetUiEvent.NextMonth) }
                    )
                }

                state.overallBudget?.let { overall ->
                    item { BudgetOverviewCard(overall) }
                }

                if (state.categoryBudgets.isNotEmpty()) {
                    item {
                        ZenSectionHeader(
                            title = stringResource(R.string.budgets_categories),
                            modifier = Modifier.padding(top = Dimensions.SpacingMedium)
                        )
                    }
                    items(state.categoryBudgets.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
                        ) {
                            pair.forEach { budget -> CategoryBudgetTile(budget) }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                } else if (state.overallBudget == null) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.budgets_empty_title),
                            message = stringResource(R.string.budgets_empty_body),
                            action = {
                                ZenPrimaryButton(
                                    text = stringResource(R.string.budgets_add),
                                    onClick = onNavigateToAdd
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Hero tile: ring + spent/limit. Also reused by Insights → Budgets tab. */
@Composable
fun BudgetOverviewCard(progress: BudgetProgress, modifier: Modifier = Modifier) {
    val over = progress.isOverBudget
    ZenTile(
        modifier = modifier.fillMaxWidth(),
        tone = ZenTone.Lime,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge - 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ZenRingProgress(
                progress = progress.progress,
                progressColor = if (over) ZenSpendTheme.colors.expense else Color.Unspecified
            ) {
                Text(
                    text = "${(progress.progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimensions.SpacingLarge)
            ) {
                ZenTileCaption(stringResource(R.string.budgets_monthly))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(progress.spentAmount)),
                    style = AmountLarge
                )
                ZenTileCaption(
                    stringResource(
                        R.string.budgets_spent_of,
                        stringResource(R.string.amount_rupees, formatMinorUnits(progress.budget.amount))
                    )
                )
                Spacer(Modifier.height(Dimensions.SpacingXSmall))
                Text(
                    text = if (over) {
                        stringResource(
                            R.string.budgets_over_by,
                            stringResource(R.string.amount_rupees, formatMinorUnits(abs(progress.remainingAmount)))
                        )
                    } else {
                        stringResource(
                            R.string.budgets_left,
                            stringResource(R.string.amount_rupees, formatMinorUnits(progress.remainingAmount))
                        )
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RowScope.CategoryBudgetTile(progress: BudgetProgress) {
    val name = progress.categoryName ?: stringResource(R.string.unknown)
    val over = progress.isOverBudget
    ZenTile(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        tone = ZenTone.forKey(name)
    ) {
        Text(text = name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        ZenTileValue(
            text = stringResource(R.string.amount_rupees, formatMinorUnits(progress.spentAmount)),
            style = AmountMedium
        )
        ZenTileCaption(
            stringResource(
                R.string.budgets_spent_of,
                stringResource(R.string.amount_rupees, formatMinorUnits(progress.budget.amount))
            )
        )
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenProgressBar(progress = progress.progress)
        if (over) {
            Spacer(Modifier.height(Dimensions.SpacingSmall))
            OverTag()
        }
    }
}

@Composable
private fun OverTag() {
    val ink = ZenSpendTheme.colors.tone(ZenTone.Ink)
    val coral = ZenSpendTheme.colors.tone(ZenTone.Coral)
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(ink.container)
            .padding(horizontal = Dimensions.SpacingSmall + 2.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.budgets_over_tag),
            style = MaterialTheme.typography.labelSmall,
            color = coral.container
        )
    }
}
