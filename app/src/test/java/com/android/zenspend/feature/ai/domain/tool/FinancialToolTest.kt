package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.report.domain.model.FinancialOverview
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialToolTest {

    private val reportRepository = mockk<ReportRepository>()
    private val getMonthlySummaryTool = GetMonthlySummaryTool(reportRepository)

    @Test
    fun `test GetMonthlySummaryTool returns correct structured data`() = runTest {
        val overview = FinancialOverview(
            totalIncome = 5000000L,
            totalExpense = 3200000L,
            savings = 1800000L,
            savingsRate = 36f,
            comparison = null
        )
        
        coEvery { reportRepository.getFinancialOverview(any(), any(), any(), any()) } returns flowOf(overview)

        val result = getMonthlySummaryTool.execute(mapOf("period" to "this_month"))

        assertEquals("GetMonthlySummary", result.toolName)
        assertTrue(result.data.contains("5000000"))
        assertTrue(result.data.contains("3200000"))
        assertTrue(result.data.contains("1800000"))
    }
}
