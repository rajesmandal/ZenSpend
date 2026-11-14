package com.android.zenspend.feature.household.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.usecase.GetCurrentUserUseCase
import com.android.zenspend.core.auth.domain.usecase.ObserveAuthStateUseCase
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.auth.domain.usecase.SignOutUseCase
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.core.household.InviteLinkHolder
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
    /** Either a string resource or, for server-provided validation text, the raw message. */
    val errorRes: Int? = null,
    val errorRaw: String? = null,
    val successRes: Int? = null
) {
    val hasError: Boolean get() = errorRes != null || errorRaw != null
}

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
    private val signOutUseCase: SignOutUseCase,
    private val inviteLinkHolder: InviteLinkHolder,
    val entitlementManager: com.android.zenspend.core.monetization.domain.EntitlementManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseholdUiState())
    val uiState: StateFlow<HouseholdUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            inviteLinkHolder.token.filterNotNull().collect { token ->
                _uiState.value = _uiState.value.copy(inviteToken = token)
                inviteLinkHolder.consume()
            }
        }
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
            // Once per household id (not per Room emission: the refresh itself rewrites the cache),
            // to pick up joins / removals made on the other phone.
            householdRepository.observeActiveHousehold()
                .map { it?.id }
                .filterNotNull()
                .distinctUntilChanged()
                .collect { householdRepository.refreshActiveHousehold(it) }
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorRaw = null, successRes = null)
            when (val result = createHouseholdUseCase(_uiState.value.householdName, user.userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successRes = R.string.cashbook_msg_created
                    )
                    syncScheduler.scheduleSync()
                }
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    fun createInvitation() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            val household = _uiState.value.household ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorRaw = null, successRes = null)
            when (val result = createInvitationUseCase(household.id, user.userId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastInvitation = result.data,
                    successRes = R.string.cashbook_msg_invite_ready
                )
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    fun joinHousehold() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorRaw = null, successRes = null)
            when (val result = joinHouseholdUseCase(_uiState.value.inviteToken)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, inviteToken = "", successRes = R.string.cashbook_msg_joined)
                    syncScheduler.scheduleSync()
                }
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    fun leaveHousehold() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            val household = _uiState.value.household ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorRaw = null, successRes = null)
            when (val result = householdRepository.leaveHousehold(household.id, user.userId)) {
                is Resource.Success -> {
                    scopeManager.setActiveHouseholdId(null)
                    _uiState.value = _uiState.value.copy(isLoading = false, successRes = R.string.cashbook_msg_left)
                }
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    fun removeMember(memberUserId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            val household = _uiState.value.household ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null, errorRaw = null, successRes = null)
            when (val result = householdRepository.removeMember(household.id, memberUserId, user.userId)) {
                is Resource.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, successRes = R.string.cashbook_msg_removed)
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    fun renameHousehold(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val household = _uiState.value.household ?: return@launch
            when (val result = householdRepository.renameHousehold(household.id, name)) {
                is Resource.Success -> _uiState.value =
                    _uiState.value.copy(errorRes = null, errorRaw = null, successRes = R.string.cashbook_msg_renamed)
                is Resource.Error -> fail(result.error)
                Resource.Loading -> Unit
            }
        }
    }

    /** The share sheet was shown; stop re-sharing the same invitation. */
    fun invitationShared() {
        _uiState.value = _uiState.value.copy(lastInvitation = null)
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(errorRes = null, errorRaw = null, successRes = null)
    }

    fun signOut() {
        viewModelScope.launch {
            householdRepository.clearLocalHouseholdCache()
            scopeManager.setActiveHouseholdId(null)
            signOutUseCase()
        }
    }

    private fun fail(error: AppError) {
        val raw = (error as? AppError.ValidationError.GenericError)?.message
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorRaw = raw,
            errorRes = if (raw == null) R.string.cashbook_msg_failed else null
        )
    }
}
