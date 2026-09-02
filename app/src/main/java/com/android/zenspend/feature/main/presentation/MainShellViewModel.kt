package com.android.zenspend.feature.main.presentation

import androidx.lifecycle.ViewModel
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.core.sync.domain.SyncStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainShellViewModel @Inject constructor(
    val syncStatusManager: SyncStatusManager,
    val scopeManager: HouseholdScopeManager
) : ViewModel()
