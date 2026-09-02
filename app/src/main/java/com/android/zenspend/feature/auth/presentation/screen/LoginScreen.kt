package com.android.zenspend.feature.auth.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.ui.designsystem.component.ZenGoogleSignInButton
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.feature.auth.presentation.viewmodel.LoginViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authState) {
        if (state.authState is AuthState.Authenticated) {
            onAuthenticated()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = "Sign In",
                subtitle = "Shared Cashbook",
                showBack = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ZenSpend",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
            Text(
                text = "Your calm, intelligent money companion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingXXLarge))
            Text(
                text = "Sign in to create or join a shared cashbook with your partner.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))
            ZenGoogleSignInButton(
                onClick = viewModel::signInWithGoogle,
                isLoading = state.isLoading
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                ZenStatusBanner(message = error, isError = true)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    com.android.zenspend.core.ui.theme.ZenSpendTheme {
        Column(
            modifier = Modifier.padding(Dimensions.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ZenSpend", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(Dimensions.SpacingXLarge))
            ZenGoogleSignInButton(onClick = {})
        }
    }
}
