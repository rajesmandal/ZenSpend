package com.android.zenspend.core.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface Navigator {
    val navEvents: Flow<NavEvent>
    fun navigate(destination: Destination)
    fun navigateUp()
}

sealed interface NavEvent {
    data class To(val destination: Destination) : NavEvent
    object Up : NavEvent
}

class DefaultNavigator : Navigator {
    private val _navEvents = MutableSharedFlow<NavEvent>(extraBufferCapacity = 1)
    override val navEvents: Flow<NavEvent> = _navEvents

    override fun navigate(destination: Destination) {
        _navEvents.tryEmit(NavEvent.To(destination))
    }

    override fun navigateUp() {
        _navEvents.tryEmit(NavEvent.Up)
    }
}
