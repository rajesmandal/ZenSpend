package com.android.zenspend.feature.emi.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.emi.domain.usecase.CloseEmiUseCase
import com.android.zenspend.feature.emi.domain.usecase.DeleteEmiUseCase
import com.android.zenspend.feature.emi.domain.usecase.RecordEmiInstallmentPaidUseCase
import com.android.zenspend.feature.emi.domain.usecase.UndoLastEmiPaymentUseCase
import com.android.zenspend.feature.emi.presentation.contract.EMIDetailsUiEffect
import com.android.zenspend.feature.emi.presentation.contract.EMIDetailsUiEvent
import com.android.zenspend.feature.emi.presentation.contract.EMIDetailsUiState
import com.android.zenspend.feature.emi.presentation.contract.PayWalletOption
import com.android.zenspend.feature.wallet.domain.usecase.ObserveWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EMIDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val emiRepository: EMIRepository,
    observeWallets: ObserveWalletsUseCase,
    private val recordPaid: RecordEmiInstallmentPaidUseCase,
    private val undoLast: UndoLastEmiPaymentUseCase,
    private val closeEmi: CloseEmiUseCase,
    private val deleteEmi: DeleteEmiUseCase
) : ViewModel() {

    private val emiId: Long = savedStateHandle.get<String>(EMI_ID_ARG)?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(EMIDetailsUiState())
    val uiState: StateFlow<EMIDetailsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EMIDetailsUiEffect>()
    val uiEffect: Flow<EMIDetailsUiEffect> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                emiRepository.observeEMI(emiId),
                emiRepository.observePayments(emiId),
                observeWallets()
            ) { emi, payments, wallets ->
                EMIDetailsUiState(
                    isLoading = false,
                    emi = emi,
                    payments = payments,
                    wallets = wallets.map { PayWalletOption(it.id, it.name) }
                )
            }.collect { _uiState.value = it }
        }
    }

    fun handleEvent(event: EMIDetailsUiEvent) {
        when (event) {
            is EMIDetailsUiEvent.MarkPaid -> run(R.string.emi_msg_paid) { recordPaid(emiId, event.walletId) }
            EMIDetailsUiEvent.UndoLast -> run(R.string.emi_msg_undone) { undoLast(emiId) }
            EMIDetailsUiEvent.Close -> run(R.string.emi_msg_closed) { closeEmi(emiId) }
            EMIDetailsUiEvent.Delete -> viewModelScope.launch {
                try {
                    deleteEmi(emiId)
                    _uiEffect.send(EMIDetailsUiEffect.NavigateBack)
                } catch (e: Exception) {
                    _uiEffect.send(EMIDetailsUiEffect.ShowMessage(R.string.emi_msg_failed))
                }
            }
        }
    }

    private fun run(successMessage: Int, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _uiEffect.send(EMIDetailsUiEffect.ShowMessage(successMessage))
            } catch (e: Exception) {
                _uiEffect.send(EMIDetailsUiEffect.ShowMessage(R.string.emi_msg_failed))
            }
        }
    }

    companion object {
        /** Must match the placeholder in `Destination.EmiDetails.ROUTE`. */
        const val EMI_ID_ARG = "emiId"
    }
}
