package com.android.zenspend.feature.backup.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupSyncMetadata(
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: String = "SYNCED"
)
