package com.android.zenspend.feature.category.domain.usecase

import com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao
import com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity
import com.android.zenspend.feature.category.domain.suggestion.MerchantNormalizer
import javax.inject.Inject

class UpdateMerchantPreferenceUseCase @Inject constructor(
    private val merchantPreferenceDao: MerchantCategoryPreferenceDao
) {
    suspend operator fun invoke(merchant: String?, title: String?, categoryId: Long?) {
        val normalized = MerchantNormalizer.normalize(merchant ?: title) ?: return
        if (categoryId == null || categoryId == 0L) return

        val existing = merchantPreferenceDao.getPreference(normalized)
        if (existing != null) {
            if (existing.categoryId == categoryId) {
                // Consistent preference: increase usage count
                merchantPreferenceDao.updatePreference(
                    existing.copy(
                        usageCount = existing.usageCount + 1,
                        lastUsedAt = System.currentTimeMillis()
                    )
                )
            } else {
                // Different preference: reset count or slowly migrate? 
                // For V1, we reset count to favor the newest choice after a threshold.
                merchantPreferenceDao.insertPreference(
                    MerchantCategoryPreferenceEntity(
                        normalizedMerchant = normalized,
                        categoryId = categoryId,
                        usageCount = 1,
                        lastUsedAt = System.currentTimeMillis()
                    )
                )
            }
        } else {
            // New preference
            merchantPreferenceDao.insertPreference(
                MerchantCategoryPreferenceEntity(
                    normalizedMerchant = normalized,
                    categoryId = categoryId,
                    usageCount = 1,
                    lastUsedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
