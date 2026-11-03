package com.android.zenspend.feature.sms.data.parser

import com.android.zenspend.core.database.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserTest {

    @Test
    fun `test debit sms parsing`() {
        val message = "Your A/c XXXX1234 debited by Rs.1,250 at AMAZON on 12-08-2026."
        val result = SmsParser.parse(message, "BANK", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(125000L, result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertEquals("1234", result?.accountLast4)
        assertEquals("AMAZON", result?.merchant)
    }

    @Test
    fun `test upi debit parsing`() {
        val message = "UPI transaction of Rs 850.50 debited from A/c XXXX9999 to SWIGGY."
        val result = SmsParser.parse(message, "UPI", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(85050L, result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertEquals("9999", result?.accountLast4)
        assertEquals("SWIGGY", result?.merchant)
    }

    @Test
    fun `test credit sms parsing`() {
        val message = "Rs 50,000 credited to your account XXXX1111 from Salary."
        val result = SmsParser.parse(message, "BANK", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(5000000L, result?.amount)
        assertEquals(TransactionType.INCOME, result?.type)
        assertEquals("1111", result?.accountLast4)
    }

    @Test
    fun `test credit card spent parsing`() {
        val message = "HDFC Bank: Rs 20,000 spent on Credit Card ending 1234 at FLIPKART."
        val result = SmsParser.parse(message, "HDFC", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(2000000L, result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertEquals("1234", result?.accountLast4)
        assertEquals("FLIPKART", result?.merchant)
    }

    @Test
    fun `test card payment parsing`() {
        val message = "Payment of Rs 8,000 received towards HDFC Credit Card XXXX1234."
        val result = SmsParser.parse(message, "HDFC", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(800000L, result?.amount)
        assertEquals(TransactionType.CREDIT_CARD_PAYMENT, result?.type)
        assertEquals("1234", result?.accountLast4)
    }

    @Test
    fun `test refund parsing`() {
        val message = "Refund of Rs 2,000 from AMAZON credited to your account XXXX1234."
        val result = SmsParser.parse(message, "BANK", 1723456789000L)
        
        assertNotNull(result)
        assertEquals(200000L, result?.amount)
        assertEquals(TransactionType.REFUND, result?.type)
        assertEquals("1234", result?.accountLast4)
        assertEquals("AMAZON", result?.merchant)
    }

    @Test
    fun `otp and promo messages are ignored`() {
        assertNull(SmsParser.parse("123456 is your OTP for txn of Rs 500. Do not share.", "BANK", 1L))
        assertNull(SmsParser.parse("Get pre-approved loan up to Rs 5,00,000 today", "BANK", 1L))
    }

    @Test
    fun `first direction word wins when message has debit and credit`() {
        val message = "Rs 700 debited from A/c XX4321 and credited to beneficiary RAHUL."
        val result = SmsParser.parse(message, "BANK", 1L)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertEquals(70000L, result?.amount)
    }

    @Test
    fun `credit card spend is an expense not a credit`() {
        val message = "Rs 1,499.00 spent on your SBI Credit Card ending 8899 at MYNTRA on 01-11-26."
        val result = SmsParser.parse(message, "SBICRD", 1L)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertEquals(149900L, result?.amount)
        assertEquals("8899", result?.accountLast4)
        assertEquals("MYNTRA", result?.merchant)
    }

    @Test
    fun `money received from a person is income not card payment`() {
        val message = "Rs 500 received in A/c XX1234 from RAMESH towards rent."
        assertEquals(TransactionType.INCOME, SmsParser.parse(message, "BANK", 1L)?.type)
    }
}
