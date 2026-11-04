package com.android.zenspend.feature.budget.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetPlanTest {

    private val salary = 50_000_00L // ₹50,000 in paise

    @Test
    fun `50-30-20 splits the salary`() {
        val result = BudgetPlanCalculator.calculate(salary, BudgetSplit.Balanced, emptyMap())
        assertEquals(25_000_00L, result.needs)
        assertEquals(15_000_00L, result.wants)
        assertEquals(10_000_00L, result.savings)
        assertEquals(40_000_00L, result.spendingCap)
    }

    @Test
    fun `buckets always add up to the income even with rounding`() {
        val income = 100_01L
        val result = BudgetPlanCalculator.calculate(income, BudgetSplit(33, 33, 34), emptyMap())
        assertEquals(income, result.needs + result.wants + result.savings)
    }

    @Test
    fun `categories share their bucket equally and keep every paisa`() {
        val assignments = mapOf(
            1L to BudgetBucket.NEEDS,
            2L to BudgetBucket.NEEDS,
            3L to BudgetBucket.NEEDS,
            4L to BudgetBucket.WANTS
        )
        val result = BudgetPlanCalculator.calculate(100_00L, BudgetSplit.Balanced, assignments)
        // Needs = 50.00 across 3 categories -> 16.67, 16.67, 16.66
        assertEquals(listOf(16_67L, 16_67L, 16_66L), listOf(1L, 2L, 3L).map { result.categoryAmounts.getValue(it) })
        assertEquals(result.needs, listOf(1L, 2L, 3L).sumOf { result.categoryAmounts.getValue(it) })
        assertEquals(result.wants, result.categoryAmounts.getValue(4L))
    }

    @Test
    fun `savings categories get no budget`() {
        val result = BudgetPlanCalculator.calculate(salary, BudgetSplit.Balanced, mapOf(9L to BudgetBucket.SAVINGS))
        assertTrue(result.categoryAmounts.isEmpty())
    }

    @Test
    fun `zero or negative income gives zero everywhere`() {
        val result = BudgetPlanCalculator.calculate(-5L, BudgetSplit.Balanced, mapOf(1L to BudgetBucket.NEEDS))
        assertEquals(0L, result.spendingCap + result.savings)
        assertEquals(0L, result.categoryAmounts.getValue(1L))
    }

    @Test
    fun `adjusting needs takes from savings and keeps the total at 100`() {
        val split = BudgetSplit.Balanced.adjust(BudgetBucket.NEEDS, 5)
        assertEquals(BudgetSplit(55, 30, 15), split)
        assertTrue(split.isValid)
    }

    @Test
    fun `adjusting savings moves the difference to wants`() {
        assertEquals(BudgetSplit(50, 25, 25), BudgetSplit.Balanced.adjust(BudgetBucket.SAVINGS, 5))
    }

    @Test
    fun `adjustment is clamped so no bucket goes negative`() {
        val split = BudgetSplit(50, 30, 20).adjust(BudgetBucket.NEEDS, 500)
        assertEquals(BudgetSplit(70, 30, 0), split)
        assertEquals(BudgetSplit(0, 30, 70), split.adjust(BudgetBucket.NEEDS, -500))
    }

    @Test
    fun `every preset is valid and the first one is 50-30-20`() {
        assertTrue(BudgetSplit.presets.all { it.isValid })
        assertEquals("50/30/20", BudgetSplit.presets.first().label)
        assertFalse(BudgetSplit(50, 30, 30).isValid)
    }

    @Test
    fun `classifier puts rent and bills in needs and unknown names in wants`() {
        assertEquals(BudgetBucket.NEEDS, CategoryBucketClassifier.classify("House Rent"))
        assertEquals(BudgetBucket.NEEDS, CategoryBucketClassifier.classify("Electricity Bill"))
        assertEquals(BudgetBucket.NEEDS, CategoryBucketClassifier.classify("Groceries"))
        assertEquals(BudgetBucket.WANTS, CategoryBucketClassifier.classify("Netflix"))
        assertEquals(BudgetBucket.WANTS, CategoryBucketClassifier.classify("Shopping"))
    }
}
