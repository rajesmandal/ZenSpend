package com.android.zenspend.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.zenspend.core.database.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType,
    val isActive: Boolean = true,
    val isDefault: Boolean = false
)
