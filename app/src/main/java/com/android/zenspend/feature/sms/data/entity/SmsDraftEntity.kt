package com.android.zenspend.feature.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.TransactionType

@Entity(
    tableName = "sms_drafts",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["status"])
    ]
)
data class SmsDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long, // paise
    val type: TransactionType,
    val date: Long,
    val merchant: String?,
    val accountLast4: String?,
    val sender: String,
    val fingerprint: String, // Unique hash for deduplication
    val status: SmsDraftStatus = SmsDraftStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SmsDraftStatus {
    PENDING,
    IGNORED,
    COMMITTED
}
