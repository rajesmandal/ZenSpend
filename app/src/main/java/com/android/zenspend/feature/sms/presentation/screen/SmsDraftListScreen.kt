package com.android.zenspend.feature.sms.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSecondaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.AmountHero
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftListEvent
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsDraftListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: (Long) -> Unit,
    viewModel: SmsDraftListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.sms_title),
                showBack = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.onEvent(SmsDraftListEvent.SyncHistoricalRequested) }) {
                        Icon(Icons.Default.Sync, contentDescription = stringResource(R.string.sms_sync))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.drafts.isEmpty() -> EmptyState(
                title = stringResource(R.string.sms_title),
                message = stringResource(R.string.sms_empty),
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
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
                    ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Sun) {
                        ZenTileCaption(stringResource(R.string.sms_waiting))
                        ZenTileValue(
                            text = pluralStringResource(R.plurals.sms_draft_count, state.drafts.size, state.drafts.size),
                            style = AmountHero
                        )
                    }
                }
                items(state.drafts, key = { it.id }) { draft ->
                    SmsDraftCard(
                        draft = draft,
                        onReview = { onNavigateToReview(draft.id) },
                        onIgnore = { viewModel.onEvent(SmsDraftListEvent.IgnoreRequested(draft.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmsDraftCard(
    draft: SmsTransactionDraft,
    onReview: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val merchant = draft.merchant ?: stringResource(R.string.unknown)
    val isIncome = draft.type == TransactionType.INCOME
    val dateText = remember(draft.date) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(draft.date))
    }
    val amountColor = if (isIncome) ZenSpendTheme.colors.income else ZenSpendTheme.colors.expense

    ZenTile(modifier = modifier.fillMaxWidth(), tone = ZenTone.Surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenBadge(label = merchant)
            Column(modifier = Modifier.weight(1f)) {
                Text(merchant, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                ZenTileCaption(dateText)
            }
            val amountRes = if (isIncome) R.string.amount_rupees_income else R.string.amount_rupees_expense
            ZenTileValue(
                text = stringResource(amountRes, formatMinorUnits(draft.amount)),
                style = AmountMedium,
                color = amountColor
            )
        }
        ZenTileCaption(
            text = stringResource(
                R.string.sms_account_line,
                draft.type.name,
                draft.accountLast4 ?: stringResource(R.string.sms_account_unknown)
            ),
            modifier = Modifier.padding(top = Dimensions.SpacingMedium)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
        ) {
            ZenSecondaryButton(
                text = stringResource(R.string.action_ignore),
                onClick = onIgnore,
                modifier = Modifier.weight(1f)
            )
            ZenPrimaryButton(
                text = stringResource(R.string.sms_review),
                onClick = onReview,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
