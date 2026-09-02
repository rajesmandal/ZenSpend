package com.android.zenspend.feature.premium.presentation.screen

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.core.monetization.domain.model.BillingProduct
import com.android.zenspend.core.monetization.domain.model.SubscriptionState
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.premium.presentation.viewmodel.PremiumViewModel

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
                title = "ZenSpend Premium",
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.SectionGap)
        ) {
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                Text(
                    text = "Upgrade Your Experience",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock AI insights, advanced reports, and an ad-free experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Dimensions.SpacingSmall)
                )
            }

            item {
                ZenSurfaceCard {
                    PremiumBenefits()
                }
            }

            if (state.subscriptionState is SubscriptionState.Premium) {
                item {
                    ZenStatusBanner(message = "You are a Premium member — thank you!")
                }
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
                        Text("Already purchased? Restore Purchases")
                    }
                }
            }

            item {
                Text(
                    text = "Subscriptions renew automatically unless cancelled in Google Play. Your financial data is always accessible.",
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
        BenefitItem("Unlimited AI Financial Assistant")
        BenefitItem("Smart AI Transaction Categorization")
        BenefitItem("Advanced Reports & Multi-period Trends")
        BenefitItem("No Ads — Clean Focused Experience")
        BenefitItem("Shared Household Cashbook")
        BenefitItem("Priority Support & Future Premium Tools")
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
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)) {
        ZenSurfaceCard(onClick = onMonthlyClick) {
            Text("Monthly Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("₹199 / month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ZenSurfaceCard(onClick = onYearlyClick) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Yearly Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("₹1,499 / year", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Best value",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
