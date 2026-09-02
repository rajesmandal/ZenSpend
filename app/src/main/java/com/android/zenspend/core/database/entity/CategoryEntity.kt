package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.CategoryType
import com.android.zenspend.core.database.model.SyncState

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType,
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
