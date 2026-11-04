package com.android.zenspend.feature.budget.domain.model

/** The three buckets of a percentage-based budget plan (50/30/20 style). */
enum class BudgetBucket { NEEDS, WANTS, SAVINGS }

/**
 * How income is divided between the buckets, in whole percent. Always sums to 100 when built through
 * [adjust] or [presets]; use [isValid] when the values come from storage.
 */
data class BudgetSplit(
    val needsPercent: Int,
    val wantsPercent: Int,
    val savingsPercent: Int
) {
    val total: Int get() = needsPercent + wantsPercent + savingsPercent
    val isValid: Boolean
        get() = total == FULL && needsPercent >= 0 && wantsPercent >= 0 && savingsPercent >= 0

    /** "50/30/20" */
    val label: String get() = "$needsPercent/$wantsPercent/$savingsPercent"

    fun percentOf(bucket: BudgetBucket): Int = when (bucket) {
        BudgetBucket.NEEDS -> needsPercent
        BudgetBucket.WANTS -> wantsPercent
        BudgetBucket.SAVINGS -> savingsPercent
    }

    /**
     * Moves [bucket] by [delta] percent and rebalances so the total stays 100. Needs/Wants take from
     * (or give to) Savings; Savings takes from (or gives to) Wants. The change is clamped so that no
     * bucket goes below 0.
     */
    fun adjust(bucket: BudgetBucket, delta: Int): BudgetSplit {
        val counter = if (bucket == BudgetBucket.SAVINGS) BudgetBucket.WANTS else BudgetBucket.SAVINGS
        val allowed = delta.coerceIn(-percentOf(bucket), percentOf(counter))
        return withPercent(bucket, percentOf(bucket) + allowed)
            .withPercent(counter, percentOf(counter) - allowed)
    }

    private fun withPercent(bucket: BudgetBucket, percent: Int): BudgetSplit = when (bucket) {
        BudgetBucket.NEEDS -> copy(needsPercent = percent)
        BudgetBucket.WANTS -> copy(wantsPercent = percent)
        BudgetBucket.SAVINGS -> copy(savingsPercent = percent)
    }

    companion object {
        const val FULL = 100
        const val STEP = 5

        /** The classic rule — the default. */
        val Balanced = BudgetSplit(50, 30, 20)

        /** Ready-made splits offered as chips. Add or reorder here; the UI follows. */
        val presets: List<BudgetSplit> = listOf(
            Balanced,
            BudgetSplit(60, 20, 20),
            BudgetSplit(70, 20, 10),
            BudgetSplit(50, 20, 30)
        )
    }
}

/** What the plan works out to, in minor units (paise). */
data class BudgetPlanResult(
    val needs: Long,
    val wants: Long,
    val savings: Long,
    /** Per-category monthly budget amounts, for the categories that were assigned a bucket. */
    val categoryAmounts: Map<Long, Long>
) {
    /** Everything that may be spent this month (needs + wants); the overall budget. */
    val spendingCap: Long get() = needs + wants

    fun amountOf(bucket: BudgetBucket): Long = when (bucket) {
        BudgetBucket.NEEDS -> needs
        BudgetBucket.WANTS -> wants
        BudgetBucket.SAVINGS -> savings
    }
}

/** Pure maths of the plan, kept free of Android so it is trivially unit-testable. */
object BudgetPlanCalculator {

    /**
     * @param income monthly take-home income in minor units.
     * @param assignments category id -> bucket. Categories mapped to [BudgetBucket.SAVINGS] get no
     * budget (savings is a target, not spending).
     */
    fun calculate(
        income: Long,
        split: BudgetSplit,
        assignments: Map<Long, BudgetBucket>
    ): BudgetPlanResult {
        val safeIncome = income.coerceAtLeast(0L)
        val needs = safeIncome * split.needsPercent / BudgetSplit.FULL
        val wants = safeIncome * split.wantsPercent / BudgetSplit.FULL
        // Whatever rounding leaves over goes to savings so the three always add up to the income.
        val savings = safeIncome - needs - wants

        val amounts = LinkedHashMap<Long, Long>()
        amounts += splitEqually(needs, assignments.idsIn(BudgetBucket.NEEDS))
        amounts += splitEqually(wants, assignments.idsIn(BudgetBucket.WANTS))
        return BudgetPlanResult(needs, wants, savings, amounts)
    }

    private fun Map<Long, BudgetBucket>.idsIn(bucket: BudgetBucket): List<Long> =
        filterValues { it == bucket }.keys.sorted()

    /** Splits [total] across [ids]; the first ids get the extra paise so nothing is lost. */
    private fun splitEqually(total: Long, ids: List<Long>): Map<Long, Long> {
        if (ids.isEmpty()) return emptyMap()
        val base = total / ids.size
        val remainder = (total % ids.size).toInt()
        return ids.mapIndexed { index, id -> id to base + if (index < remainder) 1 else 0 }.toMap()
    }
}
