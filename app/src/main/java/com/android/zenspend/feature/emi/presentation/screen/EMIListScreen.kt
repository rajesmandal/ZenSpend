package com.android.zenspend.feature.emi.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.EmptyState
import com.android.zenspend.core.ui.designsystem.component.LoadingState
import com.android.zenspend.core.ui.designsystem.component.ZenPrimaryButton
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.designsystem.component.ZenTopAppBar
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.presentation.viewmodel.EMIViewModel

@Composable
fun EMIListScreen(
    onNavigateToAdd: () -> Unit,
    viewModel: EMIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ZenTopAppBar(
                title = stringResource(R.string.emi_title),
                actions = {
                    if (state.emis.isNotEmpty()) {
                        IconButton(onClick = onNavigateToAdd) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cards_add_emi))
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.emis.isEmpty() -> EmptyState(
                title = stringResource(R.string.emi_empty_title),
                message = stringResource(R.string.emi_empty_body),
                icon = Icons.Default.ReceiptLong,
                action = { ZenPrimaryButton(text = stringResource(R.string.cards_add_emi), onClick = onNavigateToAdd) },
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.ScreenPadding,
                    vertical = Dimensions.SpacingLarge
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
            ) {
                items(state.emis, key = { it.id }) { emi -> EMIItem(emi = emi) }
            }
        }
    }
}

@Composable
fun EMIItem(emi: EMI, modifier: Modifier = Modifier) {
    val progress = if (emi.totalTenure > 0) {
        (emi.totalTenure - emi.remainingInstallments).toFloat() / emi.totalTenure
    } else {
        0f
    }

    ZenTile(modifier = modifier.fillMaxWidth(), tone = ZenTone.forKey(emi.id)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(emi.name, style = MaterialTheme.typography.titleMedium)
                ZenTileCaption(
                    stringResource(R.string.cards_emi_months_left, emi.remainingInstallments, emi.totalTenure)
                )
            }
            ZenTileValue(text = stringResource(R.string.cards_emi_per_month, formatMinorUnits(emi.emiAmount)))
        }
        ZenProgressBar(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.SpacingMedium)
        )
    }
}
