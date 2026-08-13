package com.android.zenspend.feature.transaction.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.zenspend.core.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterBottomSheet(
    onDismiss: () -> Unit,
    // Add filter logic
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.SpacingLarge)
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            Text("Filtering logic will be fully implemented in the final production UI.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge))
        }
    }
}
