package com.android.zenspend.feature.category.domain.model

import com.android.zenspend.core.database.model.CategoryType

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType,
    val isActive: Boolean = true,
    val isDefault: Boolean = false
)
