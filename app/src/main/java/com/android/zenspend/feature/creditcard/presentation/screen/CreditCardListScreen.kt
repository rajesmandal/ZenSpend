package com.android.zenspend.feature.creditcard.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.creditcard.presentation.model.CreditCardUiModel
import com.android.zenspend.feature.creditcard.presentation.viewmodel.CreditCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Credit Cards", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.cards.isEmpty()) {
            EmptyState(
                title = "No Credit Cards",
                message = "Add your credit cards to track outstanding and limits",
                icon = Icons.Default.CreditCard,
                action = { ZenPrimaryButton(text = "Add Card", onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)
            ) {
                items(state.cards) { card ->
                    CreditCardItem(
                        card = card,
                        onClick = { onNavigateToDetails(card.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreditCardItem(
    card: CreditCardUiModel,
    onClick: () -> Unit
) {
    ZenCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(Dimensions.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(card.bankName, style = MaterialTheme.typography.labelMedium)
                    Text(card.cardName, style = MaterialTheme.typography.titleLarge)
                }
                Text("**** ${card.lastFourDigits}", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Outstanding", style = MaterialTheme.typography.labelSmall)
                    Text("₹${card.outstanding}", style = MaterialTheme.typography.headlineSmall, color = ZenSpendTheme.colors.expense)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available Limit", style = MaterialTheme.typography.labelSmall)
                    Text("₹${card.availableLimit}", style = MaterialTheme.typography.titleMedium, color = ZenSpendTheme.colors.income)
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            
            LinearProgressIndicator(
                progress = { card.utilization.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (card.utilization > 0.8f) ZenSpendTheme.colors.expense else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
