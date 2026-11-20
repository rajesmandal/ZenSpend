package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A group that shares costs (trip, PG, dinner). Personal to this device, not in the household ledger. */
@Entity(tableName = "split_groups")
data class SplitGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "split_members",
    foreignKeys = [
        ForeignKey(
            entity = SplitGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class SplitMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val isMe: Boolean = false
)

@Entity(
    tableName = "split_expenses",
    foreignKeys = [
        ForeignKey(
            entity = SplitGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val amount: Long,
    val paidBy: Long,
    val date: Long,
    /** [com.android.zenspend.feature.split.domain.model.SplitType] name. */
    val splitType: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** What one member consumed of one expense. Removed together with the expense. */
@Entity(
    tableName = "split_shares",
    foreignKeys = [
        ForeignKey(
            entity = SplitExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"])]
)
data class SplitShareEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseId: Long,
    val memberId: Long,
    val amount: Long
)

@Entity(
    tableName = "split_settlements",
    foreignKeys = [
        ForeignKey(
            entity = SplitGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class SplitSettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val fromMember: Long,
    val toMember: Long,
    val amount: Long,
    val date: Long
)
