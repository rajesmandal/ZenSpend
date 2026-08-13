package com.android.zenspend.feature.category.domain.usecase

import com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao
import com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateMerchantPreferenceUseCaseTest {

    private val merchantPreferenceDao = mockk<MerchantCategoryPreferenceDao>(relaxed = true)
    private val useCase = UpdateMerchantPreferenceUseCase(merchantPreferenceDao)

    @Test
    fun `test increment usage count for same category`() = runTest {
        val merchant = "AMAZON"
        val categoryId = 1L
        val existing = MerchantCategoryPreferenceEntity(merchant, categoryId, 2)
        
        coEvery { merchantPreferenceDao.getPreference(merchant) } returns existing

        useCase(merchant, null, categoryId)

        coVerify { 
            merchantPreferenceDao.updatePreference(match { it.usageCount == 3 && it.categoryId == categoryId })
        }
    }

    @Test
    fun `test reset usage count for different category`() = runTest {
        val merchant = "AMAZON"
        val oldCategoryId = 1L
        val newCategoryId = 2L
        val existing = MerchantCategoryPreferenceEntity(merchant, oldCategoryId, 5)
        
        coEvery { merchantPreferenceDao.getPreference(merchant) } returns existing

        useCase(merchant, null, newCategoryId)

        coVerify { 
            merchantPreferenceDao.insertPreference(match { it.usageCount == 1 && it.categoryId == newCategoryId })
        }
    }
}
