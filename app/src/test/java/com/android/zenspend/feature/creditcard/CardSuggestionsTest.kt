package com.android.zenspend.feature.creditcard

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.suggestion.BankSenders
import com.android.zenspend.feature.creditcard.domain.suggestion.CardSuggestions
import com.android.zenspend.feature.sms.data.parser.SmsParser
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardSuggestionsTest {

    private fun draft(last4: String?, isCard: Boolean = true, sender: String = "AD-HDFCBK-S", amount: Long = 100_00) =
        SmsTransactionDraft(
            amount = amount, type = TransactionType.EXPENSE, date = 0, merchant = "X",
            accountLast4 = last4, sender = sender, fingerprint = "f$last4$amount$sender", isCard = isCard
        )

    private fun card(last4: String) = CreditCard(
        walletId = 1, name = "c", bankName = "HDFC Bank", lastFourDigits = last4,
        creditLimit = 1, billingCycleDate = 1, paymentDueDate = 1
    )

    @Test fun groupsByLast4AndNamesTheBank() {
        val result = CardSuggestions.from(listOf(draft("1234"), draft("1234", amount = 50_00), draft("9999", sender = "VM-ICICIB")), emptyList())
        assertEquals(listOf("1234", "9999"), result.map { it.last4 })
        assertEquals(2, result[0].transactionCount)
        assertEquals(150_00L, result[0].totalAmount)
        assertEquals("HDFC Bank", result[0].bankName)
        assertEquals("ICICI Bank", result[1].bankName)
    }

    @Test fun skipsKnownCardsDismissedAndNonCardSms() {
        val drafts = listOf(draft("1234"), draft("5555"), draft("7777"), draft("8888", isCard = false), draft(null))
        val result = CardSuggestions.from(drafts, listOf(card("1234")), dismissed = setOf("5555"))
        assertEquals(listOf("7777"), result.map { it.last4 })
    }

    @Test fun unknownSenderGivesNoBank() = assertNull(BankSenders.bankName("XY-RANDOM-S"))

    @Test fun parserFlagsCreditCardMessagesOnly() {
        assertTrue(SmsParser.isCardMessage("Rs 500 spent on HDFC Bank Credit Card XX1234 at AMAZON"))
        assertTrue(SmsParser.isCardMessage("Spent Rs.500 on card ending 4321 at SWIGGY"))
        assertFalse(SmsParser.isCardMessage("Rs 500 debited from A/c XX1234 to Raj"))
        assertFalse(SmsParser.isCardMessage("Rs 500 spent on Debit Card XX1234 at SHOP"))
    }
}
