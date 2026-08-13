package com.android.zenspend.feature.category.data.mapper

import com.android.zenspend.core.database.entity.CategoryEntity
import com.android.zenspend.feature.category.domain.model.Category

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isActive = isActive,
    isDefault = isDefault
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isActive = isActive,
    isDefault = isDefault
)
