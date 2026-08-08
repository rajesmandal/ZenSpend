package com.android.zenspend.core.common.error

sealed interface AppError {
    sealed interface DatabaseError : AppError {
        object ReadError : DatabaseError
        object WriteError : DatabaseError
        data class GenericError(val message: String) : DatabaseError
    }

    sealed interface NetworkError : AppError {
        object NoInternet : NetworkError
        object ServerError : NetworkError
        data class ApiError(val code: Int, val message: String) : NetworkError
    }

    data class UnexpectedError(val throwable: Throwable) : AppError
}
