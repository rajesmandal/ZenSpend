package com.android.zenspend.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.zenspend.R
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.ui.designsystem.component.ZenGoogleSignInButton
import com.android.zenspend.core.ui.designsystem.component.ZenStatusBanner
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authState) {
        if (state.authState is AuthState.Authenticated) onAuthenticated()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }

            BrandTile()

            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.TileGap)) {
                FeatureTile(
                    tone = ZenTone.Mint,
                    icon = Icons.Default.AccountBalanceWallet,
                    label = stringResource(R.string.login_feature_track),
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    tone = ZenTone.Lilac,
                    icon = Icons.Default.PieChart,
                    label = stringResource(R.string.login_feature_budget),
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    tone = ZenTone.Sky,
                    icon = Icons.Default.AutoAwesome,
                    label = stringResource(R.string.login_feature_ai),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.login_body),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.SpacingSmall)
            )

            ZenGoogleSignInButton(
                onClick = viewModel::signInWithGoogle,
                isLoading = state.isLoading
            )
            state.errorMessage?.let { ZenStatusBanner(message = it, isError = true) }

            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.login_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(Dimensions.SpacingLarge))
        }
    }
}

@Composable
private fun BrandTile() {
    ZenTile(
        modifier = Modifier.fillMaxWidth(),
        tone = ZenTone.Ink,
        contentPadding = PaddingValues(Dimensions.SpacingXLarge)
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(Dimensions.LoginLogoSize)
                .clip(RoundedCornerShape(Dimensions.RadiusCard))
        )
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        Text(
            text = stringResource(R.string.login_headline),
            style = AmountLarge,
            color = ZenSpendTheme.colors.tone(ZenTone.Lime).container
        )
        Spacer(Modifier.height(Dimensions.SpacingSmall))
        ZenTileCaption(stringResource(R.string.login_tagline))
    }
}

@Composable
private fun FeatureTile(tone: ZenTone, icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    ZenTile(modifier = modifier, tone = tone, contentPadding = PaddingValues(Dimensions.SpacingLarge)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 2)
    }
}
