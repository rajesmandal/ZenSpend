package com.android.zenspend.core.sync.domain.model

data class InitialSyncProgress(
    val entityType: String,
    val downloaded: Int,
    val total: Int?,
    val isComplete: Boolean = false
)
