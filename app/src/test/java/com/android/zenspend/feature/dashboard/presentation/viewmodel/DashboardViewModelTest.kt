package com.android.zenspend.feature.dashboard.presentation.viewmodel

import com.android.zenspend.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.android.zenspend.feature.dashboard.presentation.contract.DashboardUiEvent
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase = mockk()
    private val adController: com.android.zenspend.core.monetization.presentation.AdController = mockk(relaxed = true)
    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeDashboardSummaryUseCase(any(), any()) } returns flowOf(mockk())
        viewModel = DashboardViewModel(observeDashboardSummaryUseCase, adController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should trigger summary observation`() = runTest {
        val state = viewModel.uiState.value
        // Verify initial month is current
        val now = Calendar.getInstance()
        assertEquals(now.get(Calendar.MONTH), state.selectedMonth.get(Calendar.MONTH))
    }

    @Test
    fun `select month event should change selected month`() = runTest {
        val target = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }

        viewModel.handleEvent(DashboardUiEvent.SelectMonth(target))

        val selected = viewModel.uiState.value.selectedMonth
        assertEquals(target.get(Calendar.MONTH), selected.get(Calendar.MONTH))
        assertEquals(target.get(Calendar.YEAR), selected.get(Calendar.YEAR))
    }
}
