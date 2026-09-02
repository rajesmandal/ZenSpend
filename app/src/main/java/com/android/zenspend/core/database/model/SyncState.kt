package com.android.zenspend.core.database.model

enum class SyncState {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    FAILED
}
