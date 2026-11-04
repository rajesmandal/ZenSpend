package com.android.zenspend.feature.ai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.PremiumFeatureGate
import com.android.zenspend.core.ui.designsystem.component.ZenChip
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.InputShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.ai.domain.model.AiMessage
import com.android.zenspend.feature.ai.domain.model.AiRole
import com.android.zenspend.feature.ai.presentation.viewmodel.AiAssistantEvent
import com.android.zenspend.feature.ai.presentation.viewmodel.AiAssistantViewModel

/** Chat bubble corner that points at the speaker. */
private val BubbleTailRadius = 6.dp

/** Suggestion prompts shown above the input. Labels are string resources. */
private val suggestedQuestionRes = listOf(
    R.string.ai_q_spent,
    R.string.ai_q_biggest,
    R.string.ai_q_saved,
    R.string.ai_q_balance,
    R.string.ai_q_category
)

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.ai_title),
                showBack = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        PremiumFeatureGate(
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
                        .padding(horizontal = Dimensions.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
                    contentPadding = PaddingValues(vertical = Dimensions.SpacingLarge)
                ) {
                    if (state.messages.isEmpty()) {
                        item { WelcomeSection() }
                    }

                    items(state.messages) { message -> AiMessageBubble(message) }

                    if (state.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                CircularProgressIndicator(modifier = Modifier.size(Dimensions.IconSizeMedium))
                            }
                        }
                    }

                    state.error?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = Dimensions.SpacingSmall)
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
fun AiMessageBubble(message: AiMessage, modifier: Modifier = Modifier) {
    val isUser = message.role == AiRole.USER
    // User = Ink bubble, assistant = Sky bubble: both come from tones, not raw colours.
    val tone = ZenSpendTheme.colors.tone(if (isUser) ZenTone.Ink else ZenTone.Sky)
    val big = Dimensions.RadiusTile
    val shape = RoundedCornerShape(
        topStart = big,
        topEnd = big,
        bottomStart = if (isUser) big else BubbleTailRadius,
        bottomEnd = if (isUser) BubbleTailRadius else big
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = Dimensions.ChatBubbleMaxWidth),
            color = tone.container,
            contentColor = tone.content,
            shape = shape
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingMedium),
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
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val canSend = enabled && query.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ai_placeholder)) },
            maxLines = 3,
            enabled = enabled,
            shape = InputShape
        )
        val send = ZenSpendTheme.colors.tone(if (canSend) ZenTone.Lime else ZenTone.Surface)
        Surface(
            shape = CircleShape,
            color = send.container,
            contentColor = send.content
        ) {
            IconButton(onClick = onSend, enabled = canSend, modifier = Modifier.size(Dimensions.MinTouchTarget)) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.action_send))
            }
        }
    }
}

@Composable
fun WelcomeSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ai_welcome_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.ai_welcome_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimensions.SpacingSmall)
        )
    }
}

@Composable
fun SuggestedQuestions(onQuestionClick: (String) -> Unit) {
    val questions = suggestedQuestionRes.map { stringResource(it) }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimensions.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        items(questions) { question ->
            ZenChip(label = question, selected = false, onClick = { onQuestionClick(question) })
        }
    }
}

@Composable
fun AiPrivacyDisclaimer(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.ai_privacy_title)) },
        text = { Text(stringResource(R.string.ai_privacy_body)) },
        confirmButton = {
            TextButton(onClick = onAccept) { Text(stringResource(R.string.ai_privacy_accept)) }
        }
    )
}
