package com.android.zenspend.feature.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.usecase.*
import com.android.zenspend.feature.wallet.presentation.contract.*
import com.android.zenspend.feature.wallet.presentation.model.WalletUiModel
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val addWalletUseCase: AddWalletUseCase,
    private val archiveWalletUseCase: ArchiveWalletUseCase,
    private val getWalletUseCase: GetWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<WalletUiEffect>()
    val uiEffect: Flow<WalletUiEffect> = _uiEffect.receiveAsFlow()

    init {
        handleEvent(WalletUiEvent.LoadWallets)
    }

    fun handleEvent(event: WalletUiEvent) {
        when (event) {
            is WalletUiEvent.LoadWallets -> observeWallets()
            is WalletUiEvent.LoadWalletDetails -> loadWalletDetails(event.id)
            is WalletUiEvent.LoadWalletForEdit -> loadWalletForEdit(event.id)
            is WalletUiEvent.OnNameChange -> _uiState.update { 
                it.copy(formState = it.formState.copy(name = event.name, nameError = null)) 
            }
            is WalletUiEvent.OnTypeChange -> _uiState.update { 
                it.copy(formState = it.formState.copy(type = event.type)) 
            }
            is WalletUiEvent.OnBalanceChange -> _uiState.update { 
                it.copy(formState = it.formState.copy(openingBalanceText = event.balance, balanceError = null)) 
            }
            is WalletUiEvent.SaveWallet -> saveWallet()
            is WalletUiEvent.ArchiveWallet -> archiveWallet(event.id)
        }
    }

    private fun observeWallets() {
        observeWalletsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { wallets ->
                val uiModels = wallets.map { it.toUi() }
                val (assets, liabilities) = uiModels.partition { !it.isLiability }
                
                val assetSum = assets.sumOf { it.rawBalance }
                val liabilitySum = liabilities.sumOf { it.rawBalance }
                val total = assetSum + liabilitySum

                _uiState.update { it.copy(
                    isLoading = false, 
                    wallets = uiModels,
                    assetBalance = formatAmount(assetSum),
                    liabilityBalance = formatAmount(liabilitySum.coerceAtMost(0) * -1),
                    totalBalance = formatAmount(total)
                ) }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWalletDetails(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(detailsState = WalletDetailsState.Loading) }
            val wallet = getWalletUseCase(id)
            if (wallet == null) {
                _uiState.update { it.copy(detailsState = WalletDetailsState.Error("Wallet not found")) }
                return@launch
            }

            transactionRepository.getTransactionsByWallet(id)
                .map { transactions -> transactions.map { it.toUi() } }
                .collect { txs ->
                    _uiState.update { it.copy(
                        detailsState = WalletDetailsState.Success(wallet.toUi(), txs)
                    ) }
                }
        }
    }

    private fun loadWalletForEdit(id: Long) {
        viewModelScope.launch {
            val wallet = getWalletUseCase(id)
            wallet?.let { w ->
                _uiState.update { 
                    it.copy(formState = WalletFormState(
                        id = w.id,
                        name = w.name,
                        type = w.type,
                        openingBalanceText = (w.openingBalance.toDouble() / 100.0).toString(),
                        isEditMode = true
                    ))
                }
            }
        }
    }

    private fun saveWallet() {
        val form = _uiState.value.formState
        if (form.name.isBlank()) {
            _uiState.update { it.copy(formState = it.formState.copy(nameError = "Name cannot be empty")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = true)) }
            val amount = form.openingBalanceText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            
            val wallet = Wallet(
                id = form.id,
                name = form.name,
                type = form.type,
                openingBalance = amount,
                currentBalance = amount
            )

            try {
                if (form.isEditMode) {
                    updateWalletUseCase(wallet)
                } else {
                    addWalletUseCase(wallet)
                }
                _uiEffect.send(WalletUiEffect.ShowSnackbar("Wallet saved successfully"))
                _uiEffect.send(WalletUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiEffect.send(WalletUiEffect.ShowSnackbar("Failed to save wallet"))
            } finally {
                _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = false)) }
            }
        }
    }

    private fun archiveWallet(id: Long) {
        viewModelScope.launch {
            try {
                archiveWalletUseCase(id)
                _uiEffect.send(WalletUiEffect.ShowSnackbar("Wallet archived"))
                _uiEffect.send(WalletUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiEffect.send(WalletUiEffect.ShowSnackbar("Failed to archive wallet"))
            }
        }
    }

    private fun formatAmount(amount: Long): String {
        return "%.2f".format(amount.toDouble() / 100.0)
    }

    private fun Wallet.toUi(): WalletUiModel {
        return WalletUiModel(
            id = id,
            name = name,
            type = type,
            balance = formatAmount(currentBalance),
            rawBalance = currentBalance,
            currency = currency,
            isActive = isActive
        )
    }

    private fun com.android.zenspend.feature.transaction.domain.model.Transaction.toUi(): TransactionUiModel {
        return TransactionUiModel(
            id = id,
            title = title,
            amount = formatAmount(amount),
            date = "$date",
            type = type,
            categoryName = categoryId?.toString() ?: "General",
            walletName = "",
            note = note
        )
    }
}
