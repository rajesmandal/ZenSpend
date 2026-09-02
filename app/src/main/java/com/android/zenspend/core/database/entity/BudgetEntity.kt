package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.BudgetPeriod
import com.android.zenspend.core.database.model.SyncState

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["householdId"]),
        Index(value = ["remoteId"]),
        Index(value = ["syncState"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long?,
    val amount: Long,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val householdId: String? = null,
    val createdByUserId: String? = null,
    val updatedByUserId: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
