package com.android.zenspend.feature.ai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.feature.ai.domain.model.AiMessage
import com.android.zenspend.feature.ai.domain.model.AiRole
import com.android.zenspend.feature.ai.presentation.viewmodel.AiAssistantEvent
import com.android.zenspend.feature.ai.presentation.viewmodel.AiAssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (!state.isDisclaimerAccepted) {
        AiPrivacyDisclaimer(onAccept = { viewModel.handleEvent(AiAssistantEvent.OnDisclaimerAccepted) })
    }

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = "Ask ZenSpend",
                showBack = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        com.android.zenspend.core.ui.designsystem.component.PremiumFeatureGate(
            entitlementManager = viewModel.entitlementManager,
            onUpgradeClick = onNavigateToPremium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            WelcomeSection()
                        }
                    }

                    items(state.messages) { message ->
                        AiMessageBubble(message)
                    }

                    if (state.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (state.error != null) {
                        item {
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                SuggestedQuestions(
                    onQuestionClick = { viewModel.handleEvent(AiAssistantEvent.OnSuggestedQuestionClicked(it)) }
                )

                ChatInput(
                    query = state.query,
                    onQueryChange = { viewModel.handleEvent(AiAssistantEvent.OnQueryChanged(it)) },
                    onSend = { viewModel.handleEvent(AiAssistantEvent.OnSendClicked) },
                    enabled = !state.isLoading
                )
            }
        }
    }
}

@Composable
fun AiMessageBubble(message: AiMessage) {
    val isUser = message.role == AiRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ChatInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything about your finances...") },
                maxLines = 3,
                enabled = enabled,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = enabled && query.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (enabled && query.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to ZenSpend AI",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "I can help you analyze your spending, check balances, and track budgets. Ask me anything!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun SuggestedQuestions(onQuestionClick: (String) -> Unit) {
    val questions = listOf(
        "How much did I spend this month?",
        "What was my biggest expense?",
        "How much did I save?",
        "What's my balance?",
        "Which category cost me the most?"
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(questions) { question ->
            SuggestionChip(
                onClick = { onQuestionClick(question) },
                label = { Text(question) }
            )
        }
    }
}

@Composable
fun AiPrivacyDisclaimer(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Privacy & AI") },
        text = {
            Text(
                "ZenSpend AI processes your financial data locally on this device to answer your questions.\n\n" +
                "• Only anonymous aggregate totals for your specific query are shared with the AI service.\n" +
                "• No personally identifiable information (PII), card numbers, or raw transcripts are ever shared.\n" +
                "• The AI is read-only and cannot modify your financial records."
            )
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("I Understand")
            }
        }
    )
}
