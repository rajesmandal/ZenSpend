package com.android.zenspend.feature.category.data.suggestion

import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.core.database.entity.CategoryEntity
import com.android.zenspend.core.database.model.CategoryType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao
import com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity
import com.android.zenspend.feature.category.domain.suggestion.SuggestionSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalCategorySuggestionEngineTest {

    private val merchantPreferenceDao = mockk<MerchantCategoryPreferenceDao>()
    private val categoryDao = mockk<CategoryDao>()
    private val engine = LocalCategorySuggestionEngine(merchantPreferenceDao, categoryDao)

    private val categories = listOf(
        CategoryEntity(1, "Shopping", null, null, CategoryType.EXPENSE),
        CategoryEntity(2, "Food", null, null, CategoryType.EXPENSE),
        CategoryEntity(3, "Groceries", null, null, CategoryType.EXPENSE)
    )

    @Test
    fun `test merchant rule suggestion`() = runTest {
        coEvery { categoryDao.getAllActiveCategories() } returns flowOf(categories)
        coEvery { merchantPreferenceDao.getPreference(any()) } returns null

        val result = engine.getSuggestion("AMAZON", null, TransactionType.EXPENSE)

        assertNotNull(result)
        assertEquals(1L, result?.categoryId)
        assertEquals(SuggestionSource.RULE, result?.source)
    }

    @Test
    fun `test user history override suggestion`() = runTest {
        coEvery { categoryDao.getAllActiveCategories() } returns flowOf(categories)
        // User confirmed Amazon as Groceries (3) multiple times
        coEvery { merchantPreferenceDao.getPreference("AMAZON") } returns MerchantCategoryPreferenceEntity("AMAZON", 3, 5)

        val result = engine.getSuggestion("AMAZON", null, TransactionType.EXPENSE)

        assertNotNull(result)
        assertEquals(3L, result?.categoryId)
        assertEquals(SuggestionSource.USER_HISTORY, result?.source)
    }

    @Test
    fun `test keyword rule suggestion`() = runTest {
        coEvery { categoryDao.getAllActiveCategories() } returns flowOf(categories)
        coEvery { merchantPreferenceDao.getPreference(any()) } returns null

        val result = engine.getSuggestion(null, "Fresh milk and bread", TransactionType.EXPENSE)

        assertNotNull(result)
        assertEquals(3L, result?.categoryId) // Groceries keyword MILK
    }
}
