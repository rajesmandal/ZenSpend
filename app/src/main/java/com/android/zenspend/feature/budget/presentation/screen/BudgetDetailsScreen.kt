package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.zenspend.core.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailsScreen(
    budgetId: Long,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* Delete */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.SpacingLarge)
        ) {
            Text("Budget #$budgetId", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            Text("Transaction history for this budget will be shown here in the final UI.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
