package com.android.zenspend.feature.voice.presentation.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.zenspend.feature.voice.data.manager.VoiceInputState
import com.android.zenspend.feature.voice.domain.model.ParsingStatus
import com.android.zenspend.feature.voice.domain.model.VoiceTransactionDraft
import com.android.zenspend.feature.voice.presentation.viewmodel.VoiceUiEvent
import com.android.zenspend.feature.voice.presentation.viewmodel.VoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputOverlay(
    onDismiss: () -> Unit,
    onDraftAccepted: (VoiceTransactionDraft) -> Unit,
    viewModel: VoiceViewModel
) {
    val state by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.handleEvent(VoiceUiEvent.StartListening)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Voice Transaction",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            when (val inputState = state.inputState) {
                is VoiceInputState.Idle -> {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.size(80.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Start Listening", modifier = Modifier.size(32.dp))
                    }
                    Text("Tap to speak your transaction")
                }
                is VoiceInputState.Listening -> {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp))
                    Text("Listening...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
                is VoiceInputState.Success -> {
                    Text(
                        text = "\"${inputState.transcript}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (state.isProcessing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (state.draft != null) {
                        ParsedDraftPreview(
                            draft = state.draft!!,
                            onAccept = { onDraftAccepted(state.draft!!) },
                            onRetry = { viewModel.handleEvent(VoiceUiEvent.Reset) }
                        )
                    }
                }
                is VoiceInputState.Error -> {
                    Text(text = inputState.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.handleEvent(VoiceUiEvent.Reset) }) {
                        Text("Try Again")
                    }
                }
            }
            
            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ParsedDraftPreview(
    draft: VoiceTransactionDraft,
    onAccept: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = draft.type?.name ?: "Unknown Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "₹${(draft.amount ?: 0L) / 100.0}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (draft.parsingStatus == ParsingStatus.SUCCESS) {
                Text("Ready to add!", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(
                    "Missing: ${draft.missingFields.joinToString { it.name }}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                    Text("Retry")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    enabled = draft.parsingStatus != ParsingStatus.ERROR
                ) {
                    Text("Add Details")
                }
            }
        }
    }
}
