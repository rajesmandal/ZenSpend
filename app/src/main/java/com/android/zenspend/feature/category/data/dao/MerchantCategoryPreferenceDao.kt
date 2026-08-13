package com.android.zenspend.feature.category.data.dao

import androidx.room.*
import com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity

@Dao
interface MerchantCategoryPreferenceDao {
    @Query("SELECT * FROM merchant_category_preferences WHERE normalizedMerchant = :normalizedMerchant")
    suspend fun getPreference(normalizedMerchant: String): MerchantCategoryPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: MerchantCategoryPreferenceEntity)

    @Update
    suspend fun updatePreference(preference: MerchantCategoryPreferenceEntity)

    @Query("DELETE FROM merchant_category_preferences WHERE normalizedMerchant = :normalizedMerchant")
    suspend fun deletePreference(normalizedMerchant: String)
}
