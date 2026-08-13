package com.android.zenspend.feature.sms.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftListEvent
import com.android.zenspend.feature.sms.presentation.viewmodel.SmsDraftListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDraftListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: (Long) -> Unit,
    viewModel: SmsDraftListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detected Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(SmsDraftListEvent.SyncHistoricalRequested) }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Historical")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.drafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transaction drafts found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.drafts) { draft ->
                    SmsDraftCard(
                        draft = draft,
                        onReview = { onNavigateToReview(draft.id) },
                        onIgnore = { viewModel.onEvent(SmsDraftListEvent.IgnoreRequested(draft.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmsDraftCard(
    draft: SmsTransactionDraft,
    onReview: () -> Unit,
    onIgnore: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = draft.merchant ?: "Unknown Merchant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${draft.amount / 100.0}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (draft.type == com.android.zenspend.core.database.model.TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(draft.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "${draft.type} | A/c XXXX${draft.accountLast4 ?: "****"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onIgnore) {
                    Text("Ignore")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onReview) {
                    Text("Review")
                }
            }
        }
    }
}
