package com.android.zenspend.feature.creditcard.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenStat
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.creditcard.presentation.model.CreditCardUiModel
import com.android.zenspend.feature.creditcard.presentation.viewmodel.CreditCardViewModel

/** Utilization above this fraction is shown in the "expense" colour. */
internal const val UTILIZATION_ALERT_FRACTION = 0.8f

@Composable
fun CreditCardListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.cards_title),
                subtitle = stringResource(R.string.cards_subtitle),
                actions = {
                    if (state.cards.isNotEmpty()) {
                        IconButton(onClick = onNavigateToAdd) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cards_add))
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.cards.isEmpty() -> EmptyState(
                title = stringResource(R.string.cards_empty_title),
                message = stringResource(R.string.cards_empty_body),
                icon = Icons.Default.CreditCard,
                action = { ZenPrimaryButton(text = stringResource(R.string.cards_add), onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
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
                items(state.cards, key = { it.id }) { card ->
                    CreditCardItem(card = card, onClick = { onNavigateToDetails(card.id) })
                }
            }
        }
    }
}

/** A credit card rendered as a bento tile. Its tone is derived from the card id, never hardcoded. */
@Composable
fun CreditCardItem(
    card: CreditCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expenseColor = ZenSpendTheme.colors.expense
    ZenTile(
        modifier = modifier.fillMaxWidth(),
        tone = ZenTone.forKey(card.id),
        onClick = onClick,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ZenTileCaption(card.bankName)
                Text(card.cardName, style = MaterialTheme.typography.titleLarge)
                ZenTileCaption(stringResource(R.string.cards_last_four, card.lastFourDigits))
            }
            if (card.activeEmiCount > 0) {
                val ink = ZenSpendTheme.colors.tone(ZenTone.Ink)
                Surface(shape = PillShape, color = ink.container, contentColor = ink.content) {
                    Text(
                        text = pluralStringResource(R.plurals.cards_emi_count, card.activeEmiCount, card.activeEmiCount),
                        modifier = Modifier.padding(horizontal = Dimensions.SpacingMedium, vertical = Dimensions.SpacingXSmall),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingXLarge)
        ) {
            ZenStat(
                caption = stringResource(R.string.cards_outstanding),
                value = stringResource(R.string.amount_rupees, card.outstanding),
                modifier = Modifier.weight(1f)
            )
            ZenStat(
                caption = stringResource(R.string.cards_available),
                value = stringResource(R.string.amount_rupees, card.availableLimit),
                modifier = Modifier.weight(1f)
            )
        }

        ZenProgressBar(
            progress = card.utilization.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingLarge),
            fillColor = if (card.utilization > UTILIZATION_ALERT_FRACTION) expenseColor else Color.Unspecified
        )
    }
}
