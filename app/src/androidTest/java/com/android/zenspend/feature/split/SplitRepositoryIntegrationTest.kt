package com.android.zenspend.feature.split

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.feature.split.data.SplitRepositoryImpl
import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitMath
import com.android.zenspend.feature.split.domain.model.SplitSettlement
import com.android.zenspend.feature.split.domain.model.SplitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real Room (in memory) behind the real repository: the group ledger end to end. */
@RunWith(AndroidJUnit4::class)
class SplitRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: SplitRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = SplitRepositoryImpl(db.splitDao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun trip(): Triple<Long, Long, List<Long>> {
        val groupId = repo.createGroup("Goa trip", "You", listOf("Amit", "Neha"))
        val members = repo.observeGroup(groupId).first()!!.members
        return Triple(groupId, members.first { it.isMe }.id, members.filterNot { it.isMe }.map { it.id })
    }

    private fun bill(groupId: Long, paidBy: Long, amount: Long, shares: Map<Long, Long>, id: Long = 0) = SplitExpense(
        id = id, groupId = groupId, title = "Dinner", amount = amount, paidBy = paidBy, date = 1L, shares = shares
    )

    @Test fun createGroupAddsMeFirstAndFriends() = runBlocking {
        val (groupId, me, others) = trip()
        val details = repo.observeGroup(groupId).first()!!
        assertEquals(3, details.members.size)
        assertTrue(details.members.first().isMe)
        assertEquals(me, details.me!!.id)
        assertEquals(2, others.size)
    }

    @Test fun equalSplitAmongEveryoneBalancesCorrectly() = runBlocking {
        val (groupId, me, others) = trip()
        val shares = SplitMath.equalShares(300_00, listOf(me) + others)
        assertNotNull(repo.saveExpense(bill(groupId, me, 300_00, shares)))
        val d = repo.observeGroup(groupId).first()!!
        assertEquals(200_00L, d.balances[me])
        assertEquals(-100_00L, d.balances[others[0]])
        assertEquals(0L, d.balances.values.sum())
    }

    @Test fun billForSelectedPeopleOnlyLeavesOthersUntouched() = runBlocking {
        val (groupId, me, others) = trip()
        // Amit pays 600 for Amit + Neha only; "me" is not part of it.
        val shares = SplitMath.equalShares(600_00, others)
        assertNotNull(repo.saveExpense(bill(groupId, others[0], 600_00, shares)))
        val d = repo.observeGroup(groupId).first()!!
        assertEquals(0L, d.balances[me])
        assertEquals(300_00L, d.balances[others[0]])
        assertEquals(-300_00L, d.balances[others[1]])
    }

    @Test fun editingAnExpenseReplacesItsShares() = runBlocking {
        val (groupId, me, others) = trip()
        val id = repo.saveExpense(bill(groupId, me, 300_00, SplitMath.equalShares(300_00, listOf(me) + others)))!!
        repo.saveExpense(bill(groupId, me, 200_00, SplitMath.equalShares(200_00, listOf(me, others[0])), id = id))
        val saved = repo.getExpense(id)!!
        assertEquals(200_00L, saved.amount)
        assertEquals(setOf(me, others[0]), saved.shares.keys)
        assertEquals(200_00L, saved.shares.values.sum())
        assertEquals(1, repo.observeGroup(groupId).first()!!.expenses.size)
    }

    @Test fun exactSplitIsStoredAsTyped() = runBlocking {
        val (groupId, me, others) = trip()
        val id = repo.saveExpense(
            bill(groupId, me, 500_00, mapOf(me to 100_00L, others[0] to 150_00L, others[1] to 250_00L))
                .copy(splitType = SplitType.EXACT)
        )!!
        val saved = repo.getExpense(id)!!
        assertEquals(SplitType.EXACT, saved.splitType)
        assertEquals(250_00L, saved.shares[others[1]])
    }

    @Test fun settlementClearsDebt() = runBlocking {
        val (groupId, me, others) = trip()
        repo.saveExpense(bill(groupId, me, 200_00, mapOf(me to 100_00L, others[0] to 100_00L)))
        assertTrue(repo.addSettlement(SplitSettlement(groupId = groupId, from = others[0], to = me, amount = 100_00, date = 2L)))
        val d = repo.observeGroup(groupId).first()!!
        assertTrue(d.isSettled)
        assertTrue(d.transfers.isEmpty())
    }

    @Test fun deletingAnExpenseRemovesItsShares() = runBlocking {
        val (groupId, me, others) = trip()
        val id = repo.saveExpense(bill(groupId, me, 200_00, mapOf(me to 100_00L, others[0] to 100_00L)))!!
        repo.deleteExpense(id)
        assertTrue(db.splitDao().getShares(id).isEmpty())
        assertTrue(repo.observeGroup(groupId).first()!!.isSettled)
    }

    @Test fun deletingAGroupCascadesEverything() = runBlocking {
        val (groupId, me, others) = trip()
        val id = repo.saveExpense(bill(groupId, me, 200_00, mapOf(me to 100_00L, others[0] to 100_00L)))!!
        repo.addSettlement(SplitSettlement(groupId = groupId, from = others[0], to = me, amount = 50_00, date = 2L))
        repo.deleteGroup(groupId)
        assertNull(repo.observeGroup(groupId).first())
        assertTrue(db.splitDao().getShares(id).isEmpty())
        assertNull(repo.getExpense(id))
        assertTrue(repo.observeGroups().first().isEmpty())
    }

    @Test fun memberWithActivityCannotBeRemovedButIdleOneCan() = runBlocking {
        val (groupId, me, others) = trip()
        repo.saveExpense(bill(groupId, me, 100_00, mapOf(me to 50_00L, others[0] to 50_00L)))
        assertFalse(repo.removeMember(others[0]))
        assertTrue(repo.removeMember(others[1]))
        assertEquals(2, repo.observeGroup(groupId).first()!!.members.size)
    }

    @Test fun phoneOwnerCanNeverBeRemoved() = runBlocking {
        val (groupId, me, _) = trip()
        repo.removeMember(me)
        assertNotNull(repo.observeGroup(groupId).first()!!.me)
    }

    @Test fun groupsListShowsMyBalance() = runBlocking {
        val (groupId, me, others) = trip()
        repo.saveExpense(bill(groupId, others[0], 200_00, mapOf(me to 100_00L, others[0] to 100_00L)))
        val overview = repo.observeGroups().first().single()
        assertEquals(-100_00L, overview.myBalance)
        assertEquals(3, overview.memberCount)
    }

    // ---- adversarial: the repository must refuse inconsistent data ----

    @Test fun refusesSharesThatDoNotAddUp() = runBlocking {
        val (groupId, me, others) = trip()
        assertNull(repo.saveExpense(bill(groupId, me, 300_00, mapOf(me to 100_00L, others[0] to 100_00L))))
        assertTrue(repo.observeGroup(groupId).first()!!.expenses.isEmpty())
    }

    @Test fun refusesNonPositiveAmountAndNegativeShares() = runBlocking {
        val (groupId, me, others) = trip()
        assertNull(repo.saveExpense(bill(groupId, me, 0, mapOf(me to 0L))))
        assertNull(repo.saveExpense(bill(groupId, me, -100_00, mapOf(me to -100_00L))))
        // 600 = 700 + (-100): sums right but a negative share would hide a debt
        assertNull(repo.saveExpense(bill(groupId, me, 600_00, mapOf(me to 700_00L, others[0] to -100_00L))))
    }

    @Test fun refusesPayerOrParticipantFromAnotherGroup() = runBlocking {
        val (groupId, me, _) = trip()
        val otherGroup = repo.createGroup("Flat", "You", listOf("Raj"))
        val stranger = repo.observeGroup(otherGroup).first()!!.members.first { !it.isMe }.id
        assertNull(repo.saveExpense(bill(groupId, stranger, 100_00, mapOf(me to 100_00L))))
        assertNull(repo.saveExpense(bill(groupId, me, 100_00, mapOf(stranger to 100_00L))))
    }

    @Test fun refusesEditingAnExpenseThroughAnotherGroup() = runBlocking {
        val (groupId, me, _) = trip()
        val id = repo.saveExpense(bill(groupId, me, 100_00, mapOf(me to 100_00L)))!!
        val otherGroup = repo.createGroup("Flat", "You", emptyList())
        val otherMe = repo.observeGroup(otherGroup).first()!!.me!!.id
        assertNull(repo.saveExpense(bill(otherGroup, otherMe, 5_00, mapOf(otherMe to 5_00L), id = id)))
        assertEquals(100_00L, repo.getExpense(id)!!.amount)
    }

    @Test fun refusesBadSettlements() = runBlocking {
        val (groupId, me, others) = trip()
        assertFalse(repo.addSettlement(SplitSettlement(groupId = groupId, from = me, to = me, amount = 10_00, date = 1)))
        assertFalse(repo.addSettlement(SplitSettlement(groupId = groupId, from = me, to = others[0], amount = 0, date = 1)))
        assertFalse(repo.addSettlement(SplitSettlement(groupId = groupId, from = me, to = others[0], amount = -5, date = 1)))
        assertFalse(repo.addSettlement(SplitSettlement(groupId = groupId, from = me, to = 9999, amount = 10_00, date = 1)))
        assertTrue(repo.observeGroup(groupId).first()!!.settlements.isEmpty())
    }

    @Test fun blankTitleAndNamesAreHandled() = runBlocking {
        val groupId = repo.createGroup("  Trip  ", "  ", listOf(" ", "Amit ", ""))
        val d = repo.observeGroup(groupId).first()!!
        assertEquals("Trip", d.group.name)
        assertEquals(listOf("You", "Amit"), d.members.map { it.name })
        assertFalse(repo.addMember(groupId, "   "))
        val me = d.me!!.id
        val id = repo.saveExpense(bill(groupId, me, 100_00, mapOf(me to 100_00L)).copy(title = "  "))!!
        assertEquals("Expense", repo.getExpense(id)!!.title)
    }
}
