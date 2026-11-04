package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.budget.domain.model.BudgetBucket
import com.android.zenspend.feature.budget.domain.model.BudgetSplit
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetPlanEffect
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetPlanEvent
import com.android.zenspend.feature.budget.presentation.viewmodel.BudgetPlanViewModel
import com.android.zenspend.feature.budget.presentation.viewmodel.PlanCategory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

/** Tone, title and hint for each bucket. Change the look of the planner here. */
private data class BucketStyle(val tone: ZenTone, val title: Int, val hint: Int)

private fun BudgetBucket.style() = when (this) {
    BudgetBucket.NEEDS -> BucketStyle(ZenTone.Lime, R.string.plan_needs, R.string.plan_needs_hint)
    BudgetBucket.WANTS -> BucketStyle(ZenTone.Lilac, R.string.plan_wants, R.string.plan_wants_hint)
    BudgetBucket.SAVINGS -> BucketStyle(ZenTone.Mint, R.string.plan_savings, R.string.plan_savings_hint)
}

@Composable
fun BudgetPlanScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetPlanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val failedMessage = stringResource(R.string.plan_failed)

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is BudgetPlanEffect.Applied -> onNavigateBack()
                BudgetPlanEffect.Failed -> snackbarHostState.showSnackbar(failedMessage)
            }
        }
    }

    val monthLabel = remember(state.month) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(state.month.time)
    }
    val presetLabels = BudgetSplit.presets.map { it.label } + stringResource(R.string.plan_custom)
    val result = state.result

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.plan_title),
                subtitle = stringResource(R.string.plan_subtitle),
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Dimensions.ScreenPadding,
                end = Dimensions.ScreenPadding,
                top = Dimensions.SpacingLarge,
                bottom = Dimensions.SectionGap
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
        ) {
            item {
                ZenTextField(
                    value = state.incomeText,
                    onValueChange = { viewModel.handleEvent(BudgetPlanEvent.OnIncomeChange(it)) },
                    label = stringResource(R.string.plan_income),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item {
                ZenSectionHeader(title = stringResource(R.string.plan_split))
            }
            item {
                ZenChipRow(
                    options = presetLabels,
                    selectedIndex = state.presetIndex,
                    onSelected = { viewModel.handleEvent(BudgetPlanEvent.OnPresetSelected(it)) },
                    contentPadding = PaddingValues()
                )
            }

            items(BudgetBucket.entries.toList(), key = { it.name }) { bucket ->
                BucketTile(
                    bucket = bucket,
                    percent = state.split.percentOf(bucket),
                    amount = result.amountOf(bucket),
                    onAdjust = { delta -> viewModel.handleEvent(BudgetPlanEvent.OnAdjust(bucket, delta)) }
                )
            }

            item {
                ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Ink) {
                    ZenTileCaption(stringResource(R.string.plan_spending_cap, monthLabel))
                    ZenTileValue(text = stringResource(R.string.amount_rupees, formatMinorUnits(result.spendingCap)))
                }
            }

            item {
                ZenSectionHeader(
                    title = stringResource(R.string.plan_categories),
                    modifier = Modifier.padding(top = Dimensions.SpacingSmall)
                )
            }
            if (state.categories.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.plan_no_categories),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        amount = result.categoryAmounts[category.id],
                        onBucket = { viewModel.handleEvent(BudgetPlanEvent.OnCategoryBucket(category.id, it)) }
                    )
                }
            }

            item {
                ZenPrimaryButton(
                    text = stringResource(R.string.plan_apply, monthLabel),
                    onClick = { viewModel.handleEvent(BudgetPlanEvent.Apply) },
                    enabled = state.canApply,
                    loading = state.isApplying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.SpacingMedium)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.plan_apply_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BucketTile(
    bucket: BudgetBucket,
    percent: Int,
    amount: Long,
    onAdjust: (Int) -> Unit
) {
    val style = bucket.style()
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = style.tone) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(style.title), style = MaterialTheme.typography.titleMedium)
                ZenTileCaption(stringResource(style.hint))
                ZenTileValue(
                    text = stringResource(R.string.amount_rupees, formatMinorUnits(amount)),
                    style = AmountLarge,
                    modifier = Modifier.padding(top = Dimensions.SpacingSmall)
                )
            }
            IconButton(onClick = { onAdjust(-BudgetSplit.STEP) }) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.plan_decrease))
            }
            Text(
                text = stringResource(R.string.plan_percent, percent),
                style = AmountMedium
            )
            IconButton(onClick = { onAdjust(BudgetSplit.STEP) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.plan_increase))
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: PlanCategory,
    amount: Long?,
    onBucket: (BudgetBucket) -> Unit
) {
    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                ZenTileCaption(
                    if (amount != null) {
                        stringResource(R.string.plan_category_budget, formatMinorUnits(amount))
                    } else {
                        stringResource(R.string.plan_category_no_budget)
                    }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)) {
                listOf(BudgetBucket.NEEDS, BudgetBucket.WANTS).forEach { bucket ->
                    ZenChip(
                        label = stringResource(bucket.style().title),
                        selected = category.bucket == bucket,
                        onClick = { onBucket(bucket) }
                    )
                }
            }
        }
    }
}
