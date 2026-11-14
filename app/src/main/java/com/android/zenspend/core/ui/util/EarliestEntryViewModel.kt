package com.android.zenspend.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EarliestEntryViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {
    val earliest: StateFlow<Long?> = repository.observeEarliestDate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** Date of the oldest entry for month pickers; null in previews or when there is no entry yet. */
@Composable
fun rememberEarliestEntryMillis(): Long? {
    if (LocalInspectionMode.current) return null
    val value by hiltViewModel<EarliestEntryViewModel>().earliest.collectAsState()
    return value
}
