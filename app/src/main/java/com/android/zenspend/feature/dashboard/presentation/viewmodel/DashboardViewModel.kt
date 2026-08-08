package com.android.zenspend.feature.dashboard.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.data.ai.GeminiManager
import com.android.zenspend.feature.dashboard.presentation.model.TransactionUi
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import com.android.zenspend.feature.transaction.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val geminiManager: GeminiManager,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val allTransactions = getTransactionsUseCase().map { transactions ->
        transactions.map { it.toUi() }
    }

    fun processExpenseText(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val jsonResponse = geminiManager.extractExpenseFromText(text)
                saveResponseToDb(jsonResponse)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processExpenseImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val jsonResponse = geminiManager.extractExpenseFromImage(bitmap)
                saveResponseToDb(jsonResponse)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveResponseToDb(json: JSONObject?) {
        json?.let {
            val transaction = Transaction(
                title = it.optString("title", "Unknown"),
                amount = it.optDouble("amount", 0.0),
                category = it.optString("category", "General"),
                date = System.currentTimeMillis(),
                note = ""
            )
            addTransactionUseCase(transaction)
        }
    }

    private fun Transaction.toUi(): TransactionUi {
        return TransactionUi(
            id = id,
            title = title,
            amount = "$amount", // Simple formatting for now
            category = category,
            date = "$date",   // Simple formatting for now
            note = note
        )
    }
}
