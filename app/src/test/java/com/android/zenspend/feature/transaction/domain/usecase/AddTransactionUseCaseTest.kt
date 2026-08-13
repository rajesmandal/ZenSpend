package com.android.zenspend.feature.transaction.domain.usecase

import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTransactionUseCaseTest {

    private lateinit var repository: TransactionRepository
    private lateinit var useCase: AddTransactionUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AddTransactionUseCase(repository)
    }

    @Test
    fun `when amount is zero, should return InvalidAmount error`() = runTest {
        val transaction = createBaseTransaction(amount = 0)
        val result = useCase(transaction)
        
        assertTrue(result is Resource.Error)
        assertEquals(AppError.ValidationError.InvalidAmount, (result as Resource.Error).error)
    }

    @Test
    fun `when expense lacks source wallet, should return MissingSourceWallet error`() = runTest {
        val transaction = createBaseTransaction(type = TransactionType.EXPENSE, sourceWalletId = 0)
        val result = useCase(transaction)
        
        assertTrue(result is Resource.Error)
        assertEquals(AppError.ValidationError.MissingSourceWallet, (result as Resource.Error).error)
    }

    @Test
    fun `when transfer to same wallet, should return SameWalletTransfer error`() = runTest {
        val transaction = createBaseTransaction(
            type = TransactionType.TRANSFER,
            sourceWalletId = 1,
            destinationWalletId = 1
        )
        val result = useCase(transaction)
        
        assertTrue(result is Resource.Error)
        assertEquals(AppError.ValidationError.SameWalletTransfer, (result as Resource.Error).error)
    }

    @Test
    fun `when valid transaction, should return success with id`() = runTest {
        val transaction = createBaseTransaction(amount = 1000, sourceWalletId = 1, type = TransactionType.EXPENSE)
        coEvery { repository.insertTransaction(any()) } returns 100L
        
        val result = useCase(transaction)
        
        assertTrue(result is Resource.Success)
        assertEquals(100L, (result as Resource.Success).data)
    }

    private fun createBaseTransaction(
        amount: Long = 100,
        sourceWalletId: Long = 1,
        destinationWalletId: Long? = null,
        type: TransactionType = TransactionType.EXPENSE
    ) = Transaction(
        title = "Test",
        amount = amount,
        date = System.currentTimeMillis(),
        type = type,
        sourceWalletId = sourceWalletId,
        destinationWalletId = destinationWalletId,
        note = null
    )
}
