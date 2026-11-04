package com.android.zenspend.feature.premium.presentation.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.R
import com.android.zenspend.core.monetization.domain.model.BillingProduct
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.premium.presentation.viewmodel.PremiumViewModel

/** Benefit labels, in display order. */
private val premiumBenefitRes = listOf(
    R.string.premium_benefit_ai,
    R.string.premium_benefit_categorization,
    R.string.premium_benefit_reports,
    R.string.premium_benefit_no_ads,
    R.string.premium_benefit_household,
    R.string.premium_benefit_support
)

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.premium_title),
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
        ) {
            item {
                ZenTile(
                    modifier = Modifier.fillMaxWidth(),
                    tone = ZenTone.Ink,
                    contentPadding = PaddingValues(Dimensions.SpacingXLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ZenSpendTheme.colors.tone(ZenTone.Lime).container,
                        modifier = Modifier.size(Dimensions.IconSizeLarge)
                    )
                    Text(
                        text = stringResource(R.string.premium_headline),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = Dimensions.SpacingMedium)
                    )
                    ZenTileCaption(
                        text = stringResource(R.string.premium_subtitle),
                        modifier = Modifier.padding(top = Dimensions.SpacingSmall)
                    )
                }
            }

            item {
                ZenTile(modifier = Modifier.fillMaxWidth()) { PremiumBenefits() }
            }

            if (state.subscriptionState is SubscriptionState.Premium) {
                item { ZenStatusBanner(message = stringResource(R.string.premium_member)) }
            } else {
                item {
                    SubscriptionOptions(
                        onMonthlyClick = {
                            viewModel.purchaseProduct(context as Activity, BillingProduct.PREMIUM_MONTHLY)
                        },
                        onYearlyClick = {
                            viewModel.purchaseProduct(context as Activity, BillingProduct.PREMIUM_YEARLY)
                        }
                    )
                }
                item {
                    TextButton(onClick = { viewModel.restorePurchases() }) {
                        Text(stringResource(R.string.premium_restore))
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.premium_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Dimensions.SpacingXXLarge)
                )
            }
        }
    }
}

@Composable
fun PremiumBenefits() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
        premiumBenefitRes.forEach { BenefitItem(stringResource(it)) }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimensions.IconSizeMedium)
        )
        Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SubscriptionOptions(
    onMonthlyClick: () -> Unit,
    onYearlyClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)) {
        ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Lilac, onClick = onMonthlyClick) {
            Text(stringResource(R.string.premium_monthly), style = MaterialTheme.typography.titleMedium)
            ZenTileCaption(stringResource(R.string.premium_price_monthly))
        }
        ZenTile(modifier = Modifier.fillMaxWidth(), tone = ZenTone.Lime, onClick = onYearlyClick) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.premium_yearly), style = MaterialTheme.typography.titleMedium)
                    ZenTileCaption(stringResource(R.string.premium_price_yearly))
                }
                ZenChip(label = stringResource(R.string.premium_best_value), selected = true, onClick = onYearlyClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PremiumScreenPreview() {
    ZenSpendTheme {
        PremiumBenefits()
    }
}
