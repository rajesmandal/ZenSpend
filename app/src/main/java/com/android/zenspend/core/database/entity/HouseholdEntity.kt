package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_cache")
data class HouseholdEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val ownerUserId: String,
    val memberLimit: Int = 2,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "household_members_cache",
    primaryKeys = ["householdId", "userId"]
)
data class HouseholdMemberEntity(
    val householdId: String,
    val userId: String,
    val role: String,
    val displayName: String,
    val joinedAt: Long,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "sync_cursors",
    primaryKeys = ["householdId", "entityType"]
)
data class SyncCursorEntity(
    val householdId: String,
    val entityType: String,
    val lastRemoteUpdatedAt: Long = 0L
)
