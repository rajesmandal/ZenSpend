package com.android.zenspend.feature.creditcard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.creditcard.presentation.model.CreditCardUiModel
import com.android.zenspend.feature.creditcard.presentation.viewmodel.CreditCardViewModel

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
                title = "Credit Cards",
                subtitle = "Limits, outstanding & EMIs"
            )
        },
        floatingActionButton = {
            if (state.cards.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add credit card")
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.cards.isEmpty()) {
            EmptyState(
                title = "No Credit Cards",
                message = "Add your credit cards to track outstanding, limits, and EMIs",
                icon = Icons.Default.CreditCard,
                action = { ZenPrimaryButton(text = "Add Card", onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                items(state.cards, key = { it.id }) { card ->
                    CreditCardItem(
                        card = card,
                        onClick = { onNavigateToDetails(card.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge)) }
            }
        }
    }
}

@Composable
fun CreditCardItem(
    card: CreditCardUiModel,
    onClick: () -> Unit
) {
    ZenSurfaceCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.bankName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(card.cardName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("•••• ${card.lastFourDigits}", style = MaterialTheme.typography.bodySmall)
            }
            if (card.activeEmiCount > 0) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${card.activeEmiCount} EMI${if (card.activeEmiCount == 1) "" else "s"}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Outstanding", style = MaterialTheme.typography.labelSmall)
                AmountText(amount = "₹${card.outstanding}", type = AmountType.EXPENSE)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Available", style = MaterialTheme.typography.labelSmall)
                AmountText(amount = "₹${card.availableLimit}", type = AmountType.INCOME)
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        LinearProgressIndicator(
            progress = { card.utilization.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = if (card.utilization > 0.8f) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
