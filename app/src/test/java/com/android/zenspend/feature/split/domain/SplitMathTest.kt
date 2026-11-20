package com.android.zenspend.feature.split.domain

import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitMath
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import com.android.zenspend.feature.split.domain.model.SplitTransfer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitMathTest {

    private fun expense(paidBy: Long, amount: Long, shares: Map<Long, Long>) =
        SplitExpense(id = 1, groupId = 1, title = "x", amount = amount, paidBy = paidBy, date = 0L, shares = shares)

    // ---- equal split ----

    @Test fun equalSplitDividesEvenly() {
        assertEquals(mapOf(1L to 100L, 2L to 100L, 3L to 100L), SplitMath.equalShares(300, listOf(1, 2, 3)))
    }

    @Test fun equalSplitNeverLosesAPaise() {
        val shares = SplitMath.equalShares(1000, listOf(1, 2, 3))
        assertEquals(1000L, shares.values.sum())
        // 333 + 333 + 334 in some order: nobody differs by more than one paisa
        assertTrue(shares.values.max() - shares.values.min() <= 1)
    }

    @Test fun equalSplitAmongSelectedOnly() {
        val shares = SplitMath.equalShares(900, listOf(2, 3))
        assertEquals(mapOf(2L to 450L, 3L to 450L), shares)
        assertFalse(shares.containsKey(1L))
    }

    @Test fun equalSplitOfNobodyIsEmpty() {
        assertTrue(SplitMath.equalShares(500, emptyList()).isEmpty())
    }

    @Test fun equalSplitIgnoresDuplicateIds() {
        assertEquals(mapOf(1L to 50L, 2L to 50L), SplitMath.equalShares(100, listOf(1, 1, 2)))
    }

    // ---- exact split ----

    @Test fun exactSharesMustAddUpToTotal() {
        assertTrue(SplitMath.exactMatchesTotal(500, mapOf(1L to 200L, 2L to 300L)))
        assertFalse(SplitMath.exactMatchesTotal(500, mapOf(1L to 200L, 2L to 200L)))
    }

    @Test fun exactSharesRejectNegativeAndEmpty() {
        assertFalse(SplitMath.exactMatchesTotal(500, mapOf(1L to 600L, 2L to -100L)))
        assertFalse(SplitMath.exactMatchesTotal(500, emptyMap()))
    }

    // ---- balances ----

    @Test fun payerIsOwedWhatOthersConsumed() {
        // 1 pays 300 for 1,2,3 -> 2 and 3 owe 100 each, 1 is owed 200
        val b = SplitMath.balances(
            listOf(1, 2, 3),
            listOf(expense(1, 300, mapOf(1L to 100L, 2L to 100L, 3L to 100L))),
            emptyList()
        )
        assertEquals(mapOf(1L to 200L, 2L to -100L, 3L to -100L), b)
    }

    @Test fun onePersonPaysForSelectedOnly() {
        // 1 pays 600 only for 2 and 3 (1 not part of it)
        val b = SplitMath.balances(listOf(1, 2, 3), listOf(expense(1, 600, mapOf(2L to 300L, 3L to 300L))), emptyList())
        assertEquals(mapOf(1L to 600L, 2L to -300L, 3L to -300L), b)
    }

    @Test fun balancesAlwaysSumToZero() {
        val b = SplitMath.balances(
            listOf(1, 2, 3),
            listOf(
                expense(1, 1000, SplitMath.equalShares(1000, listOf(1, 2, 3))),
                expense(2, 777, SplitMath.equalShares(777, listOf(2, 3)))
            ),
            emptyList()
        )
        assertEquals(0L, b.values.sum())
    }

    @Test fun settlementMovesBalanceTowardsZero() {
        val expenses = listOf(expense(1, 200, mapOf(1L to 100L, 2L to 100L)))
        // 2 owes 1 100; 2 pays 1 back 100
        val b = SplitMath.balances(listOf(1, 2), expenses, listOf(SplitSettlement(1, 1, from = 2, to = 1, amount = 100, date = 0)))
        assertEquals(mapOf(1L to 0L, 2L to 0L), b)
    }

    @Test fun partialSettlementLeavesRest() {
        val expenses = listOf(expense(1, 200, mapOf(1L to 100L, 2L to 100L)))
        val b = SplitMath.balances(listOf(1, 2), expenses, listOf(SplitSettlement(1, 1, from = 2, to = 1, amount = 40, date = 0)))
        assertEquals(mapOf(1L to 60L, 2L to -60L), b)
    }

    @Test fun memberWithNoActivityHasZero() {
        val b = SplitMath.balances(listOf(1, 2, 3), listOf(expense(1, 200, mapOf(1L to 100L, 2L to 100L))), emptyList())
        assertEquals(0L, b[3L])
    }

    // ---- simplify ----

    @Test fun simplifyGivesFewestTransfers() {
        // 1 is owed 200, 2 and 3 each owe 100
        val t = SplitMath.simplify(mapOf(1L to 200L, 2L to -100L, 3L to -100L))
        assertEquals(setOf(SplitTransfer(2, 1, 100), SplitTransfer(3, 1, 100)), t.toSet())
    }

    @Test fun simplifyChainsDebts() {
        // A owes B 100, B owes C 100 -> balances A -100, B 0, C +100 -> A pays C directly
        val t = SplitMath.simplify(mapOf(1L to -100L, 2L to 0L, 3L to 100L))
        assertEquals(listOf(SplitTransfer(1, 3, 100)), t)
    }

    @Test fun simplifyOfSettledGroupIsEmpty() {
        assertTrue(SplitMath.simplify(mapOf(1L to 0L, 2L to 0L)).isEmpty())
    }

    @Test fun simplifyTransfersFullyClearEveryBalance() {
        val balances = mapOf(1L to 530L, 2L to -120L, 3L to -310L, 4L to 70L, 5L to -170L)
        val after = balances.toMutableMap()
        SplitMath.simplify(balances).forEach {
            after[it.from] = after.getValue(it.from) + it.amount
            after[it.to] = after.getValue(it.to) - it.amount
        }
        assertTrue(after.values.all { it == 0L })
        assertTrue(SplitMath.simplify(balances).size <= balances.size - 1)
    }

    // ---- per-member summary helpers ----

    @Test fun myShareOfTotalSpendingOnlyCountsMyShares() {
        val exps = listOf(
            expense(1, 300, mapOf(1L to 100L, 2L to 100L, 3L to 100L)),
            expense(2, 200, mapOf(2L to 200L))
        )
        assertEquals(100L, SplitMath.shareOf(1, exps))
        assertEquals(300L, SplitMath.shareOf(2, exps))
        assertEquals(500L, SplitMath.totalSpent(exps))
    }

    @Test fun shareOfUnknownMemberIsZero() {
        assertEquals(0L, SplitMath.shareOf(9, listOf(expense(1, 100, mapOf(1L to 100L)))))
        assertNull(SplitMath.simplify(mapOf(1L to 0L)).firstOrNull())
    }
}
