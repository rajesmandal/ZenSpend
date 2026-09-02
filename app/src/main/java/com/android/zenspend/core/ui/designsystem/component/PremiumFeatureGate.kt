package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.monetization.domain.EntitlementManager

@Composable
fun PremiumFeatureGate(
    entitlementManager: EntitlementManager,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val isPremium by entitlementManager.isPremium.collectAsState(initial = false)

    if (isPremium) {
        content()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Unlock Premium Feature",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Get access to AI assistant, advanced insights and an ad-free experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onUpgradeClick) {
                    Text("Upgrade to ZenSpend Premium")
                }
            }
        }
    }
}
