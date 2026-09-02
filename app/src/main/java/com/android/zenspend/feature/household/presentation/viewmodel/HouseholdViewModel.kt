package com.android.zenspend.feature.household.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.usecase.GetCurrentUserUseCase
import com.android.zenspend.core.auth.domain.usecase.ObserveAuthStateUseCase
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.core.sync.domain.SyncScheduler
import com.android.zenspend.feature.household.domain.model.Household
import com.android.zenspend.feature.household.domain.model.HouseholdMember
import com.android.zenspend.feature.household.domain.model.Invitation
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import com.android.zenspend.feature.household.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HouseholdUiState(
    val authState: AuthState = AuthState.Unauthenticated,
    val household: Household? = null,
    val members: List<HouseholdMember> = emptyList(),
    val householdName: String = "",
    val inviteToken: String = "",
    val lastInvitation: Invitation? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class HouseholdViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createHouseholdUseCase: CreateHouseholdUseCase,
    private val createInvitationUseCase: CreateInvitationUseCase,
    private val joinHouseholdUseCase: JoinHouseholdUseCase,
    private val householdRepository: HouseholdRepository,
    private val scopeManager: HouseholdScopeManager,
    private val syncScheduler: SyncScheduler,
    val entitlementManager: com.android.zenspend.core.monetization.domain.EntitlementManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseholdUiState())
    val uiState: StateFlow<HouseholdUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { auth ->
                _uiState.value = _uiState.value.copy(authState = auth)
            }
        }
        viewModelScope.launch {
            householdRepository.observeActiveHousehold().collect { household ->
                _uiState.value = _uiState.value.copy(household = household)
                scopeManager.setActiveHouseholdId(household?.id)
            }
        }
        viewModelScope.launch {
            householdRepository.observeActiveHousehold().collect { household ->
                if (household == null) {
                    _uiState.value = _uiState.value.copy(members = emptyList())
                } else {
                    householdRepository.observeMembers(household.id).collect { members ->
                        _uiState.value = _uiState.value.copy(members = members)
                    }
                }
            }
        }
    }

    fun onHouseholdNameChange(name: String) {
        _uiState.value = _uiState.value.copy(householdName = name)
    }

    fun onInviteTokenChange(token: String) {
        _uiState.value = _uiState.value.copy(inviteToken = token)
    }

    fun createHousehold() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = createHouseholdUseCase(_uiState.value.householdName, user.userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Household created. Invite your partner!"
                    )
                    syncScheduler.scheduleSync()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.toString())
                Resource.Loading -> Unit
            }
        }
    }

    fun createInvitation() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            val household = _uiState.value.household ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = createInvitationUseCase(household.id, user.userId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastInvitation = result.data,
                    successMessage = "Invitation created"
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.toString())
                Resource.Loading -> Unit
            }
        }
    }

    fun joinHousehold() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = joinHouseholdUseCase(_uiState.value.inviteToken)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Joined household")
                    syncScheduler.scheduleSync()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.toString())
                Resource.Loading -> Unit
            }
        }
    }

    fun leaveHousehold() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            val household = _uiState.value.household ?: return@launch
            householdRepository.leaveHousehold(household.id, user.userId)
            scopeManager.setActiveHouseholdId(null)
        }
    }

    fun renameHousehold(name: String) {
        viewModelScope.launch {
            val household = _uiState.value.household ?: return@launch
            householdRepository.renameHousehold(household.id, name)
        }
    }
}
