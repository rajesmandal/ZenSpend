package com.android.zenspend.feature.category.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_category_preferences")
data class MerchantCategoryPreferenceEntity(
    @PrimaryKey
    val normalizedMerchant: String,
    val categoryId: Long,
    val usageCount: Int,
    val lastUsedAt: Long = System.currentTimeMillis()
)
