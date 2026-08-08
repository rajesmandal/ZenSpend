package com.android.zenspend.core.common.result

import com.android.zenspend.core.common.error.AppError

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val error: AppError) : Resource<Nothing>
    object Loading : Resource<Nothing>
}
