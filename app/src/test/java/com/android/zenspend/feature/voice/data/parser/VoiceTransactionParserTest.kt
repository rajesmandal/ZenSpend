package com.android.zenspend.feature.voice.data.parser

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.category.domain.model.Category
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.voice.domain.model.ParsingStatus
import com.android.zenspend.feature.voice.domain.model.VoiceField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceTransactionParserTest {

    private val wallets = listOf(
        Wallet(1, "Cash", WalletType.CASH, 0, 10000, "INR", true),
        Wallet(2, "Bank", WalletType.BANK, 0, 500000, "INR", true),
        Wallet(3, "HDFC Card", WalletType.CREDIT_CARD, 0, -20000, "INR", true)
    )

    private val categories = listOf(
        Category(1, "Grocery", null, null, com.android.zenspend.core.database.model.CategoryType.EXPENSE, true, false),
        Category(2, "Salary", null, null, com.android.zenspend.core.database.model.CategoryType.INCOME, true, false),
        Category(3, "Food", null, null, com.android.zenspend.core.database.model.CategoryType.EXPENSE, true, false)
    )

    @Test
    fun `test basic expense in cash`() {
        val transcript = "Cash me 500 rupaye grocery ka expense"
        val result = VoiceTransactionParser.parse(transcript, wallets, categories)
        
        assertEquals(50000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals(1L, result.sourceWalletId)
        assertEquals(1L, result.categoryId)
        assertEquals(ParsingStatus.SUCCESS, result.parsingStatus)
    }

    @Test
    fun `test income to bank`() {
        val transcript = "Salary of 50000 credited to Bank"
        val result = VoiceTransactionParser.parse(transcript, wallets, categories)
        
        assertEquals(5000000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals(2L, result.destinationWalletId)
        assertEquals(2L, result.categoryId)
        assertEquals(ParsingStatus.SUCCESS, result.parsingStatus)
    }

    @Test
    fun `test transfer from bank to cash`() {
        val transcript = "Bank se Cash me 2000 transfer"
        val result = VoiceTransactionParser.parse(transcript, wallets, categories)
        
        assertEquals(200000L, result.amount)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(2L, result.sourceWalletId)
        assertEquals(1L, result.destinationWalletId)
        assertEquals(ParsingStatus.SUCCESS, result.parsingStatus)
    }

    @Test
    fun `test incomplete input missing wallet`() {
        val transcript = "500 rupaye grocery kharcha"
        val result = VoiceTransactionParser.parse(transcript, wallets, categories)
        
        assertEquals(50000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertTrue(result.missingFields.contains(VoiceField.SOURCE_WALLET))
        assertEquals(ParsingStatus.INCOMPLETE, result.parsingStatus)
    }

    @Test
    fun `test ambiguous wallet match`() {
        // This is a bit forced for this mock but tests the logic
        val transcript = "Cash and Bank 500 spend"
        val result = VoiceTransactionParser.parse(transcript, wallets, categories)
        
        assertEquals(ParsingStatus.AMBIGUOUS, result.parsingStatus)
    }
}
