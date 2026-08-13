package com.android.zenspend.feature.category.domain.repository

import com.android.zenspend.feature.category.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllActiveCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category): Long
}
