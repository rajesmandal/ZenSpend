package com.android.zenspend.feature.transaction.domain.model

import com.android.zenspend.core.database.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionWalletNormalizationTest {

    private fun tx(type: TransactionType, source: Long?, dest: Long?) = Transaction(
        title = "t", amount = 100, date = 1, note = null, type = type,
        sourceWalletId = source, destinationWalletId = dest
    )

    @Test
    fun `expense only subtracts from source wallet`() {
        val n = tx(TransactionType.EXPENSE, 1, 2).withNormalizedWallets()
        assertEquals(1L, n.sourceWalletId)
        assertNull(n.destinationWalletId)
    }

    @Test
    fun `income and refund only add to destination wallet`() {
        listOf(TransactionType.INCOME, TransactionType.REFUND).forEach {
            val n = tx(it, 1, 2).withNormalizedWallets()
            assertNull(n.sourceWalletId)
            assertEquals(2L, n.destinationWalletId)
        }
    }

    @Test
    fun `card payment keeps both legs and zero ids become null`() {
        val n = tx(TransactionType.CREDIT_CARD_PAYMENT, 1, 2).withNormalizedWallets()
        assertEquals(1L, n.sourceWalletId)
        assertEquals(2L, n.destinationWalletId)
        assertNull(tx(TransactionType.EXPENSE, 0, null).withNormalizedWallets().sourceWalletId)
    }
}
