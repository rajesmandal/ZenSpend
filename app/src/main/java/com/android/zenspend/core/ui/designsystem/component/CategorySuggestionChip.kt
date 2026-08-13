package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.zenspend.feature.category.domain.suggestion.CategorySuggestion

@Composable
fun CategorySuggestionChip(
    suggestion: CategorySuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Suggest: ${suggestion.categoryName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = modifier,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            borderWidth = 1.dp
        )
    )
}
