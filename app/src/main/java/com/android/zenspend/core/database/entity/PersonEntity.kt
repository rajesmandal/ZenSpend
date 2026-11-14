package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Someone you lend money to. Personal to this device (not part of the shared household ledger). */
@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
