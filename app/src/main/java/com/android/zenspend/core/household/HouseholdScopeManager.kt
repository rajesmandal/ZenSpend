package com.android.zenspend.core.household

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class LedgerScope {
    PERSONAL,
    HOUSEHOLD
}

@Singleton
class HouseholdScopeManager @Inject constructor() {
    private val _scope = MutableStateFlow(LedgerScope.PERSONAL)
    val scope: StateFlow<LedgerScope> = _scope.asStateFlow()

    private val _activeHouseholdId = MutableStateFlow<String?>(null)
    val activeHouseholdId: StateFlow<String?> = _activeHouseholdId.asStateFlow()

    fun setScope(scope: LedgerScope) {
        _scope.value = scope
    }

    fun setActiveHouseholdId(id: String?) {
        _activeHouseholdId.value = id
        if (id == null) {
            _scope.value = LedgerScope.PERSONAL
        }
    }

    fun effectiveHouseholdFilter(): String? {
        return if (_scope.value == LedgerScope.HOUSEHOLD) _activeHouseholdId.value else null
    }

    fun isPersonalMode(): Boolean = _scope.value == LedgerScope.PERSONAL || _activeHouseholdId.value == null
}
