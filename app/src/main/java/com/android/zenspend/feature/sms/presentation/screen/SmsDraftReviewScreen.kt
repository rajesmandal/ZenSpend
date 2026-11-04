package com.android.zenspend.feature.sms.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import com.android.zenspend.core.ui.util.MoneyFormat
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.R
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.designsystem.component.CategorySuggestionChip
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import com.android.zenspend.core.ui.designsystem.component.ZenChipRow
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenSectionHeader
import com.android.zenspend.core.ui.designsystem.component.ZenTextField
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftReviewEvent
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftReviewViewModel

/** Transaction types a detected SMS can be turned into, in display order. */
private val reviewTypes = listOf(
    TransactionType.EXPENSE to R.string.sms_type_debit,
    TransactionType.INCOME to R.string.sms_type_credit,
    TransactionType.REFUND to R.string.type_refund,
    TransactionType.CREDIT_CARD_PAYMENT to R.string.sms_type_card_bill
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmsDraftReviewScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SmsDraftReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.sms_review_title),
                showBack = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val isCardPayment = state.type == TransactionType.CREDIT_CARD_PAYMENT
        val sourceWallets = if (isCardPayment) {
            state.wallets.filter { it.type != WalletType.CREDIT_CARD }
        } else {
            state.wallets
        }
        val sourceLabel = when (state.type) {
            TransactionType.EXPENSE -> R.string.sms_paid_from_bank_card
            TransactionType.CREDIT_CARD_PAYMENT -> R.string.sms_paid_from_bank
            else -> R.string.sms_credited_to
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.ScreenPadding,
                vertical = Dimensions.SpacingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            item {
                ZenTextField(
                    value = state.title,
                    onValueChange = { viewModel.onEvent(SmsDraftReviewEvent.TitleChanged(it)) },
                    label = stringResource(R.string.sms_field_title)
                )
            }
            item {
                ZenTextField(
                    value = state.amount,
                    onValueChange = { viewModel.onEvent(SmsDraftReviewEvent.AmountChanged(it)) },
                    label = stringResource(R.string.sms_field_amount),
                    visualTransformation = MoneyFormat.visualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item { ZenSectionHeader(title = stringResource(R.string.sms_transaction_type)) }
            item {
                ZenChipRow(
                    options = reviewTypes.map { stringResource(it.second) },
                    selectedIndex = reviewTypes.indexOfFirst { it.first == state.type },
                    onSelected = { viewModel.onEvent(SmsDraftReviewEvent.TypeChanged(reviewTypes[it].first)) },
                    contentPadding = PaddingValues()
                )
            }

            item { ZenSectionHeader(title = stringResource(sourceLabel)) }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    sourceWallets.forEach { wallet ->
                        ZenChip(
                            label = wallet.name,
                            selected = state.selectedWalletId == wallet.id,
                            onClick = { viewModel.onEvent(SmsDraftReviewEvent.WalletSelected(wallet.id)) }
                        )
                    }
                }
            }

            if (isCardPayment) {
                item { ZenSectionHeader(title = stringResource(R.string.sms_card_paid)) }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                    ) {
                        state.wallets.filter { it.type == WalletType.CREDIT_CARD }.forEach { wallet ->
                            ZenChip(
                                label = wallet.name,
                                selected = state.selectedDestinationWalletId == wallet.id,
                                onClick = { viewModel.onEvent(SmsDraftReviewEvent.DestinationWalletSelected(wallet.id)) }
                            )
                        }
                    }
                }
            }

            item { ZenSectionHeader(title = stringResource(R.string.add_category)) }
            item {
                state.categorySuggestion?.let { suggestion ->
                    CategorySuggestionChip(
                        suggestion = suggestion,
                        onClick = { viewModel.onEvent(SmsDraftReviewEvent.CategorySelected(suggestion.categoryId)) },
                        modifier = Modifier.padding(bottom = Dimensions.SpacingSmall)
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                ) {
                    state.categories.forEach { category ->
                        ZenChip(
                            label = category.name,
                            selected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.onEvent(SmsDraftReviewEvent.CategorySelected(category.id)) }
                        )
                    }
                }
            }

            state.error?.let { error ->
                item { Text(text = error, color = MaterialTheme.colorScheme.error) }
            }

            item {
                ZenPrimaryButton(
                    text = stringResource(R.string.sms_confirm),
                    onClick = { viewModel.onEvent(SmsDraftReviewEvent.ConfirmClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.SpacingMedium),
                    enabled = state.selectedWalletId != null &&
                        (!isCardPayment || state.selectedDestinationWalletId != null)
                )
            }
        }
    }
}
