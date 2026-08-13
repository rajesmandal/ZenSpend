package com.android.zenspend.feature.category.data.repository

import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.feature.category.data.mapper.toDomain
import com.android.zenspend.feature.category.data.mapper.toEntity
import com.android.zenspend.feature.category.domain.model.Category
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllActiveCategories(): Flow<List<Category>> = 
        categoryDao.getAllActiveCategories().map { entities -> 
            entities.map { it.toDomain() } 
        }

    override suspend fun insertCategory(category: Category): Long = 
        categoryDao.insertCategory(category.toEntity())
}
