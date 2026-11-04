package com.android.zenspend.feature.auth.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.BuildConfig
import com.android.zenspend.core.auth.data.AuthRepositoryImpl
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.usecase.ObserveAuthStateUseCase
import com.android.zenspend.core.auth.domain.usecase.SignOutUseCase
import com.android.zenspend.core.common.result.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val authState: AuthState = AuthState.Unauthenticated,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val authRepositoryImpl: AuthRepositoryImpl,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { state ->
                _uiState.value = _uiState.value.copy(authState = state, isLoading = false)
            }
        }
    }

    /** [activityContext] must be the hosting Activity (needed to show the Google account picker). */
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            if (webClientId.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In requires Firebase setup. Add google-services.json and GOOGLE_WEB_CLIENT_ID."
                )
                return@launch
            }
            when (val result = authRepositoryImpl.signInWithCredentialManager(activityContext, webClientId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false)
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.error.toString()
                )
                Resource.Loading -> Unit
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { signOutUseCase() }
    }
}
