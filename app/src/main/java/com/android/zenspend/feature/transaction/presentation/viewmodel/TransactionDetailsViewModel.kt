package com.android.zenspend.feature.transaction.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.transaction.domain.usecase.DeleteTransactionUseCase
import com.android.zenspend.feature.wallet.domain.usecase.ObserveWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TransactionDetailsUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val date: String = "",
    val note: String? = null,
    val categoryName: String = "Uncategorized",
    val walletName: String = "—",
    val error: String? = null
)

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val observeWalletsUseCase: ObserveWalletsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    fun load(transactionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val transaction = repository.getTransactionById(transactionId)
            if (transaction == null) {
                _uiState.update { it.copy(isLoading = false, error = "Transaction not found") }
                return@launch
            }

            val categories = categoryRepository.getAllActiveCategories().first()
            val wallets = observeWalletsUseCase().first()
            val categoryName = transaction.categoryId?.let { id ->
                categories.find { it.id == id }?.name
            } ?: "Uncategorized"
            val walletName = transaction.sourceWalletId?.let { id ->
                wallets.find { it.id == id }?.name
            } ?: "—"
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = transaction.title,
                    amount = (transaction.amount.toDouble() / 100.0).let { value ->
                        String.format(Locale.getDefault(), "%.2f", value)
                    },
                    type = transaction.type,
                    date = dateFormat.format(Date(transaction.date)),
                    note = transaction.note,
                    categoryName = categoryName,
                    walletName = walletName
                )
            }
        }
    }

    fun delete(transactionId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId) ?: return@launch
            deleteTransactionUseCase(transaction)
            onDeleted()
        }
    }
}
