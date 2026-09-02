package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.zenspend.core.ui.theme.CardShape
import com.android.zenspend.core.ui.theme.Dimensions

@Composable
fun ZenCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.CardElevation
            )
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.CardElevation
            )
        ) {
            content()
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    ZenCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Dimensions.SpacingLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
