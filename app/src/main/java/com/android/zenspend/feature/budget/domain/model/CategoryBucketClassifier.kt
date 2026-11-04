package com.android.zenspend.feature.budget.domain.model

/**
 * Guesses whether a spending category is a Need or a Want from its name. This is only the starting
 * point: the user can move any category in the planner and their choice is remembered.
 */
object CategoryBucketClassifier {

    /** Lower-case fragments that mark an everyday necessity. Everything else defaults to a want. */
    private val needKeywords = listOf(
        "rent", "grocer", "bill", "electric", "water", "gas", "fuel", "petrol", "diesel",
        "transport", "commute", "travel to work", "medic", "health", "pharma", "insurance",
        "emi", "loan", "education", "school", "tuition", "fee", "recharge", "utilit",
        "internet", "broadband", "mobile", "maintenance", "tax", "childcare"
    )

    fun classify(categoryName: String): BudgetBucket {
        val name = categoryName.lowercase()
        return if (needKeywords.any { name.contains(it) }) BudgetBucket.NEEDS else BudgetBucket.WANTS
    }
}
