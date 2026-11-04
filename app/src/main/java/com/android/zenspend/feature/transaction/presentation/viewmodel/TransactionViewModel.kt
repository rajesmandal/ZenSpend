package com.android.zenspend.feature.transaction.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.android.zenspend.core.ui.util.formatMinorUnits
import com.android.zenspend.core.ui.util.MoneyFormat
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.transaction.domain.usecase.*
import com.android.zenspend.feature.transaction.presentation.contract.*
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.usecase.ObserveWalletsUseCase
import com.android.zenspend.feature.wallet.presentation.model.WalletUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val categoryRepository: CategoryRepository,
    private val getCategorySuggestionUseCase: com.android.zenspend.feature.category.domain.usecase.GetCategorySuggestionUseCase,
    private val updateMerchantPreferenceUseCase: com.android.zenspend.feature.category.domain.usecase.UpdateMerchantPreferenceUseCase,
    val adController: com.android.zenspend.core.monetization.presentation.AdController
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<TransactionUiEffect>()
    val uiEffect: Flow<TransactionUiEffect> = _uiEffect.receiveAsFlow()

    private var transactionJob: Job? = null
    
    private val titleFlow = MutableStateFlow("")

    init {
        handleEvent(TransactionUiEvent.LoadTransactions)
        loadDependencies()
        observeTitleForSuggestions()
    }

    fun handleEvent(event: TransactionUiEvent) {
        when (event) {
            is TransactionUiEvent.LoadTransactions -> observeTransactions()
            is TransactionUiEvent.SearchChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(query = event.query)) }
                observeTransactions()
            }
            is TransactionUiEvent.WalletFilterChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(walletId = event.walletId)) }
                observeTransactions()
            }
            is TransactionUiEvent.CategoryFilterChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(categoryId = event.categoryId)) }
                observeTransactions()
            }
            is TransactionUiEvent.TypeFilterChanged -> {
                _uiState.update { it.copy(filter = it.filter.copy(type = event.type)) }
                observeTransactions()
            }
            
            // Form Events
            is TransactionUiEvent.OnTypeChange -> _uiState.update { it.copy(formState = it.formState.copy(type = event.type)) }
            is TransactionUiEvent.OnAmountChange -> _uiState.update { it.copy(formState = it.formState.copy(amountText = event.amount)) }
            is TransactionUiEvent.OnTitleChange -> {
                _uiState.update { it.copy(formState = it.formState.copy(title = event.title)) }
                titleFlow.value = event.title
            }
            is TransactionUiEvent.OnNoteChange -> _uiState.update { it.copy(formState = it.formState.copy(note = event.note)) }
            is TransactionUiEvent.OnDateChange -> _uiState.update { it.copy(formState = it.formState.copy(date = event.date)) }
            is TransactionUiEvent.OnSourceWalletChange -> _uiState.update { it.copy(formState = it.formState.copy(sourceWalletId = event.id)) }
            is TransactionUiEvent.OnDestinationWalletChange -> _uiState.update { it.copy(formState = it.formState.copy(destinationWalletId = event.id)) }
            is TransactionUiEvent.OnCategoryChange -> _uiState.update { it.copy(formState = it.formState.copy(categoryId = event.id)) }
            
            is TransactionUiEvent.OnVoiceDraftAccepted -> applyVoiceDraft(event.draft)
            
            is TransactionUiEvent.SaveTransaction -> saveTransaction()
            is TransactionUiEvent.DeleteTransaction -> deleteTransaction(event.id)
        }
    }

    private fun observeTitleForSuggestions() {
        titleFlow
            .debounce(500)
            .filter { it.length > 2 }
            .distinctUntilChanged()
            .onEach { title ->
                val type = _uiState.value.formState.type
                val suggestion = getCategorySuggestionUseCase(
                    merchant = title,
                    title = title,
                    type = type,
                    allowAi = false // Can enable later based on settings
                )
                _uiState.update { it.copy(categorySuggestion = suggestion) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadDependencies() {
        observeWalletsUseCase()
            .onEach { wallets -> 
                _uiState.update { it.copy(wallets = wallets.map { w -> w.toUiModel() }) } 
            }
            .launchIn(viewModelScope)

        categoryRepository.getAllActiveCategories()
            .onEach { categories -> 
                _uiState.update { it.copy(categories = categories) } 
            }
            .launchIn(viewModelScope)
    }

    private fun observeTransactions() {
        transactionJob?.cancel()
        val filter = _uiState.value.filter
        transactionJob = repository.getFilteredTransactions(
            walletId = filter.walletId,
            categoryId = filter.categoryId,
            type = filter.type,
            searchQuery = if (filter.query.isBlank()) null else filter.query,
            startDate = filter.startDate,
            endDate = filter.endDate
        ).onStart { _uiState.update { it.copy(isLoading = true) } }
            .map { list -> list.map { it.toUiModel() } }
            .onEach { list -> _uiState.update { it.copy(isLoading = false, transactions = list) } }
            .catch { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            .launchIn(viewModelScope)
    }

    private fun saveTransaction() {
        val form = _uiState.value.formState
        val amount = form.amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        
        if (amount <= 0) {
            _uiState.update { it.copy(formState = it.formState.copy(amountError = "Invalid amount")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = true)) }
            val transaction = Transaction(
                id = form.id,
                title = form.title,
                amount = amount,
                date = form.date,
                note = form.note,
                type = form.type,
                sourceWalletId = form.sourceWalletId,
                destinationWalletId = form.destinationWalletId,
                categoryId = form.categoryId,
                originalTransactionId = form.originalTransactionId
            )

            val result = if (form.isEditMode) {
                updateTransactionUseCase(transaction)
                Resource.Success(transaction.id)
            } else {
                addTransactionUseCase(transaction)
            }

            when (result) {
                is Resource.Success -> {
                    updateMerchantPreferenceUseCase(
                        merchant = form.title,
                        title = form.title,
                        categoryId = form.categoryId
                    )
                    _uiEffect.send(TransactionUiEffect.ShowSnackbar("Transaction saved"))
                    _uiEffect.send(TransactionUiEffect.NavigateBack)
                }
                is Resource.Error -> {
                    _uiEffect.send(TransactionUiEffect.ShowSnackbar("Error: ${result.error}"))
                }
                else -> {}
            }
            _uiState.update { it.copy(formState = it.formState.copy(isSubmitting = false)) }
        }
    }

    private fun applyVoiceDraft(draft: com.android.zenspend.feature.voice.domain.model.VoiceTransactionDraft) {
        _uiState.update { it.copy(
            formState = it.formState.copy(
                amountText = draft.amount?.let(MoneyFormat::rawFromMinor) ?: "",
                title = draft.merchant ?: "",
                type = draft.type ?: TransactionType.EXPENSE,
                sourceWalletId = draft.sourceWalletId ?: 0L,
                destinationWalletId = draft.destinationWalletId,
                categoryId = draft.categoryId
            )
        ) }
    }

    private fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            _uiEffect.send(TransactionUiEffect.ShowSnackbar("Delete not yet fully implemented"))
        }
    }

    private fun Transaction.toUiModel() = TransactionUiModel(
        id = id,
        title = title,
        amount = formatMinorUnits(amount),
        date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(date)),
        type = type,
        categoryName = categoryId?.toString() ?: "General",
        walletName = sourceWalletId.toString(),
        note = note
    )

    private fun Wallet.toUiModel() = WalletUiModel(
        id = id,
        name = name,
        type = type,
        balance = formatMinorUnits(currentBalance),
        rawBalance = currentBalance,
        currency = currency,
        isActive = isActive
    )
}
