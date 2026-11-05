package com.android.zenspend.core.household

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Carries an invitation token from a `zenspend://join?token=...` link to the cashbook screen. */
@Singleton
class InviteLinkHolder @Inject constructor() {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun offer(token: String) {
        _token.value = token
    }

    fun consume() {
        _token.value = null
    }
}
