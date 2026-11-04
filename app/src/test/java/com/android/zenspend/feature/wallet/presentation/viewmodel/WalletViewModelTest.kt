package com.android.zenspend.feature.wallet.presentation.viewmodel

import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.usecase.*
import com.android.zenspend.feature.wallet.presentation.contract.WalletUiEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {

    private val observeWalletsUseCase: ObserveWalletsUseCase = mockk()
    private val addWalletUseCase: AddWalletUseCase = mockk()
    private val archiveWalletUseCase: ArchiveWalletUseCase = mockk()
    private val getWalletUseCase: GetWalletUseCase = mockk()
    private val updateWalletUseCase: UpdateWalletUseCase = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private lateinit var viewModel: WalletViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeWalletsUseCase() } returns flowOf(emptyList())
        viewModel = WalletViewModel(
            observeWalletsUseCase,
            addWalletUseCase,
            archiveWalletUseCase,
            getWalletUseCase,
            updateWalletUseCase,
            transactionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should observe wallets and calculate totals`() = runTest {
        val wallets = listOf(
            Wallet(id = 1, name = "Cash", type = WalletType.CASH, openingBalance = 100000, currentBalance = 100000), // 1000.00
            Wallet(id = 2, name = "CC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = -50000) // -500.00
        )
        every { observeWalletsUseCase() } returns flowOf(wallets)
        
        viewModel.handleEvent(WalletUiEvent.LoadWallets)
        
        val state = viewModel.uiState.value
        assertEquals("1,000", state.assetBalance)
        assertEquals("500", state.liabilityBalance)
        assertEquals("500", state.totalBalance)
    }

    @Test
    fun `when name is empty, save wallet should set error`() = runTest {
        viewModel.handleEvent(WalletUiEvent.OnNameChange(""))
        viewModel.handleEvent(WalletUiEvent.SaveWallet)
        
        assertEquals("Name cannot be empty", viewModel.uiState.value.formState.nameError)
    }
}
