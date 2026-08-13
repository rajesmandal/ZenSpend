package com.android.zenspend.feature.backup.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.zenspend.feature.backup.domain.usecase.GetBackupPreviewUseCase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RestorePreviewDialog(
    preview: GetBackupPreviewUseCase.BackupPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Backup created: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(preview.createdAt))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PreviewItem("Wallets", preview.walletCount)
                PreviewItem("Transactions", preview.transactionCount)
                PreviewItem("Categories", preview.categoryCount)
                PreviewItem("Credit Cards", preview.creditCardCount)
                PreviewItem("EMIs", preview.emiCount)
                PreviewItem("Budgets", preview.budgetCount)
                PreviewItem("Recurring Rules", preview.recurringCount)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Restoring will replace all your current ZenSpend data. A safety backup of your current data will be created first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PreviewItem(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = count.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
