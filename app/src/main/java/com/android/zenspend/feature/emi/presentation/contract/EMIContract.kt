package com.android.zenspend.feature.emi.presentation.contract

import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EmiPortfolio

/** Which lenders the list shows. */
enum class EmiFilter(val lenderType: EMILenderType?) {
    ALL(null),
    CARD(EMILenderType.CREDIT_CARD),
    BANK(EMILenderType.BANK),
    PERSON(EMILenderType.PERSON),
    OTHER(EMILenderType.OTHER)
}

data class EMIUiState(
    val isLoading: Boolean = true,
    /** Every EMI, active and closed. */
    val allEmis: List<EMI> = emptyList(),
    val showClosed: Boolean = false,
    val filter: EmiFilter = EmiFilter.ALL,
    val error: String? = null
) {
    /** Totals for the active EMIs only, regardless of the filter. */
    val portfolio: EmiPortfolio get() = EmiPortfolio.of(allEmis)

    /** The EMIs for the selected tab and lender filter. */
    val visibleEmis: List<EMI>
        get() = allEmis
            .filter { isActive(it) != showClosed }
            .filter { filter.lenderType == null || it.lenderType == filter.lenderType }

    private fun isActive(emi: EMI) =
        emi.status == com.android.zenspend.core.database.model.EMIStatus.ACTIVE && emi.remainingInstallments > 0
}

sealed interface EMIUiEvent {
    object LoadEMIs : EMIUiEvent
    data class SelectFilter(val filter: EmiFilter) : EMIUiEvent
    data class ShowClosed(val closed: Boolean) : EMIUiEvent
}

sealed interface EMIUiEffect {
    data class ShowSnackbar(val message: String) : EMIUiEffect
}
