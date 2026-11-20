package com.android.zenspend.feature.split.domain.model

/** A bunch of people sharing costs: a trip, a PG / flat, a dinner. Lives on this device only. */
data class SplitGroup(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = 0L
)

/** Someone in a group. [isMe] marks the phone owner so screens can say "You". */
data class SplitMember(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val isMe: Boolean = false
)

/** How the cost of an expense is divided. */
enum class SplitType {
    /** Same amount for everyone taking part (all members, or only the selected ones). */
    EQUAL,

    /** A typed amount per person; must add up to the bill. */
    EXACT
}

/**
 * One bill. [paidBy] fronted the money; [shares] says what each participant consumed
 * (member id -> minor units) and always adds up to [amount].
 */
data class SplitExpense(
    val id: Long = 0,
    val groupId: Long,
    val title: String,
    val amount: Long,
    val paidBy: Long,
    val date: Long,
    val shares: Map<Long, Long>,
    val splitType: SplitType = SplitType.EQUAL,
    val note: String? = null
)

/** [from] handed [amount] to [to] to clear what they owed. */
data class SplitSettlement(
    val id: Long = 0,
    val groupId: Long,
    val from: Long,
    val to: Long,
    val amount: Long,
    val date: Long
)

/** A suggested payment that brings the group closer to even. */
data class SplitTransfer(val from: Long, val to: Long, val amount: Long)

/** Everything a group screen needs, assembled from the stored rows. */
data class SplitGroupDetails(
    val group: SplitGroup,
    val members: List<SplitMember>,
    val expenses: List<SplitExpense>,
    val settlements: List<SplitSettlement>
) {
    /** member id -> net money: positive = gets back, negative = owes. */
    val balances: Map<Long, Long> by lazy { SplitMath.balances(members.map { it.id }, expenses, settlements) }
    val transfers: List<SplitTransfer> by lazy { SplitMath.simplify(balances) }
    val totalSpent: Long get() = SplitMath.totalSpent(expenses)
    val isSettled: Boolean get() = balances.values.all { it == 0L }
    val me: SplitMember? get() = members.firstOrNull { it.isMe }
    fun memberName(id: Long): String? = members.firstOrNull { it.id == id }?.name
}

/** One row of the groups list: the group plus what the phone owner is owed (or owes). */
data class SplitGroupOverview(
    val details: SplitGroupDetails
) {
    val group: SplitGroup get() = details.group
    val memberCount: Int get() = details.members.size
    val myBalance: Long get() = details.me?.let { details.balances[it.id] } ?: 0L
}

/** Pure bookkeeping, no Android. Money is always minor units (paise). */
object SplitMath {

    /**
     * [total] divided equally between [memberIds]. The leftover paise (total not divisible)
     * go one each to the first members, so the shares always add up to exactly [total].
     */
    fun equalShares(total: Long, memberIds: List<Long>): Map<Long, Long> {
        val ids = memberIds.distinct()
        if (ids.isEmpty() || total < 0) return emptyMap()
        val base = total / ids.size
        val extra = (total % ids.size).toInt()
        return ids.withIndex().associate { (i, id) -> id to base + if (i < extra) 1 else 0 }
    }

    /** Typed amounts are usable only when none is negative and together they equal the bill. */
    fun exactMatchesTotal(total: Long, shares: Map<Long, Long>): Boolean =
        shares.isNotEmpty() && shares.values.all { it >= 0 } && shares.values.sum() == total

    /**
     * Net position of every member: what they paid, minus what they consumed, plus payments
     * they made to settle, minus payments they received. Positive = others owe them.
     */
    fun balances(
        memberIds: List<Long>,
        expenses: List<SplitExpense>,
        settlements: List<SplitSettlement>
    ): Map<Long, Long> {
        val net = memberIds.associateWith { 0L }.toMutableMap()
        fun add(id: Long, delta: Long) { net[id] = (net[id] ?: 0L) + delta }
        expenses.forEach { e ->
            add(e.paidBy, e.amount)
            e.shares.forEach { (id, share) -> add(id, -share) }
        }
        settlements.forEach { s ->
            add(s.from, s.amount)
            add(s.to, -s.amount)
        }
        return net
    }

    /**
     * Fewest payments that clear everything: the biggest debtor pays the biggest creditor,
     * repeat. Ties break on member id so the result is stable.
     */
    fun simplify(balances: Map<Long, Long>): List<SplitTransfer> {
        val debtors = balances.filterValues { it < 0 }.map { it.key to -it.value }
            .sortedWith(compareByDescending<Pair<Long, Long>> { it.second }.thenBy { it.first }).toMutableList()
        val creditors = balances.filterValues { it > 0 }.map { it.key to it.value }
            .sortedWith(compareByDescending<Pair<Long, Long>> { it.second }.thenBy { it.first }).toMutableList()
        val out = mutableListOf<SplitTransfer>()
        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val (debtor, owes) = debtors.first()
            val (creditor, owed) = creditors.first()
            val pay = minOf(owes, owed)
            out += SplitTransfer(debtor, creditor, pay)
            if (owes == pay) debtors.removeAt(0) else debtors[0] = debtor to (owes - pay)
            if (owed == pay) creditors.removeAt(0) else creditors[0] = creditor to (owed - pay)
            debtors.sortWith(compareByDescending<Pair<Long, Long>> { it.second }.thenBy { it.first })
            creditors.sortWith(compareByDescending<Pair<Long, Long>> { it.second }.thenBy { it.first })
        }
        return out
    }

    /** What [memberId] actually consumed across [expenses] (their own cost, not what they paid). */
    fun shareOf(memberId: Long, expenses: List<SplitExpense>): Long = expenses.sumOf { it.shares[memberId] ?: 0L }

    fun totalSpent(expenses: List<SplitExpense>): Long = expenses.sumOf { it.amount }
}
