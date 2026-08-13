package com.android.zenspend.feature.emi.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.core.ui.designsystem.component.*
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.presentation.viewmodel.EMIViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMIListScreen(
    onNavigateToAdd: () -> Unit,
    viewModel: EMIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("EMIs", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add EMI")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (state.emis.isEmpty()) {
            EmptyState(
                title = "No Active EMIs",
                message = "Add your installments here to track repayment progress",
                icon = Icons.Default.ReceiptLong,
                action = { ZenPrimaryButton(text = "Add EMI", onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimensions.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                items(state.emis) { emi ->
                    EMIItem(emi = emi)
                }
            }
        }
    }
}

@Composable
fun EMIItem(emi: EMI) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.SpacingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(emi.name, style = MaterialTheme.typography.titleMedium)
                    Text("${emi.remainingInstallments}/${emi.totalTenure} Months left", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${emi.emiAmount / 100.0}", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            
            val progress = (emi.totalTenure - emi.remainingInstallments).toFloat() / emi.totalTenure
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
