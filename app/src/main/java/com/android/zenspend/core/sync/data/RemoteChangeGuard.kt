package com.android.zenspend.core.sync.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteChangeGuard @Inject constructor() {
    @Volatile
    var isApplyingRemoteChange: Boolean = false
}
