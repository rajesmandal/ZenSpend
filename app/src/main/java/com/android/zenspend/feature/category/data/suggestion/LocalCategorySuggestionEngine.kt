package com.android.zenspend.feature.category.data.suggestion

import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao
import com.android.zenspend.feature.category.domain.suggestion.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocalCategorySuggestionEngine @Inject constructor(
    private val merchantPreferenceDao: MerchantCategoryPreferenceDao,
    private val categoryDao: CategoryDao
) : CategorySuggestionEngine {

    private val MERCHANT_RULES = mapOf(
        "AMAZON" to "Shopping",
        "FLIPKART" to "Shopping",
        "SWIGGY" to "Food",
        "ZOMATO" to "Food",
        "UBER" to "Transport",
        "OLA" to "Transport",
        "BLINKIT" to "Groceries",
        "ZEPTO" to "Groceries",
        "BIGBASKET" to "Groceries",
        "NETFLIX" to "Entertainment",
        "SPOTIFY" to "Entertainment",
        "AIRTEL" to "Utilities",
        "JIO" to "Utilities",
        "PHARMACY" to "Medical"
    )

    private val KEYWORD_RULES = mapOf(
        "GROCERY" to "Groceries",
        "FRUITS" to "Groceries",
        "VEGETABLES" to "Groceries",
        "MILK" to "Groceries",
        "DINNER" to "Food",
        "LUNCH" to "Food",
        "COFFEE" to "Food",
        "RESTAURANT" to "Food",
        "FUEL" to "Transport",
        "PETROL" to "Transport",
        "DIESEL" to "Transport",
        "ELECTRICITY" to "Utilities",
        "WATER" to "Utilities",
        "INTERNET" to "Utilities",
        "RENT" to "Housing",
        "SALARY" to "Salary",
        "CASHBACK" to "Income"
    )

    override suspend fun getSuggestion(
        merchant: String?,
        title: String?,
        type: TransactionType
    ): CategorySuggestion? {
        val normalizedMerchant = MerchantNormalizer.normalize(merchant ?: title) ?: return null
        val categories = categoryDao.getAllActiveCategories().first()

        // 1. User History
        val preference = merchantPreferenceDao.getPreference(normalizedMerchant)
        if (preference != null && preference.usageCount >= 2) {
            val category = categories.find { it.id == preference.categoryId }
            if (category != null) {
                return CategorySuggestion(
                    categoryId = category.id,
                    categoryName = category.name,
                    confidence = 0.95f,
                    source = SuggestionSource.USER_HISTORY
                )
            }
        }

        // 2. Merchant Rules
        val ruleCategoryName = MERCHANT_RULES[normalizedMerchant]
        if (ruleCategoryName != null) {
            val category = categories.find { it.name.equals(ruleCategoryName, ignoreCase = true) }
            if (category != null) {
                return CategorySuggestion(
                    categoryId = category.id,
                    categoryName = category.name,
                    confidence = 0.85f,
                    source = SuggestionSource.RULE
                )
            }
        }

        // 3. Keyword Rules
        for ((keyword, catName) in KEYWORD_RULES) {
            if (normalizedMerchant.contains(keyword)) {
                val category = categories.find { it.name.equals(catName, ignoreCase = true) }
                if (category != null) {
                    return CategorySuggestion(
                        categoryId = category.id,
                        categoryName = category.name,
                        confidence = 0.70f,
                        source = SuggestionSource.RULE
                    )
                }
            }
        }

        return null
    }
}
