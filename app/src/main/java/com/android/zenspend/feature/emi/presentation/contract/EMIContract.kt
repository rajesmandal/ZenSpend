package com.android.zenspend.feature.emi.presentation.contract

import com.android.zenspend.feature.emi.domain.model.EMI

data class EMIUiState(
    val isLoading: Boolean = false,
    val emis: List<EMI> = emptyList(),
    val error: String? = null
)

sealed interface EMIUiEvent {
    object LoadEMIs : EMIUiEvent
}

sealed interface EMIUiEffect {
    data class ShowSnackbar(val message: String) : EMIUiEffect
}
