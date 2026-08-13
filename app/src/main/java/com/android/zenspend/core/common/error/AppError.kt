package com.android.zenspend.core.common.error

sealed interface AppError {
    sealed interface DatabaseError : AppError {
        object ReadError : DatabaseError
        object WriteError : DatabaseError
        data class GenericError(val message: String) : DatabaseError
    }

    sealed interface ValidationError : AppError {
        object InvalidAmount : ValidationError
        object MissingSourceWallet : ValidationError
        object MissingDestinationWallet : ValidationError
        object SameWalletTransfer : ValidationError
        object MissingCategory : ValidationError
        data class GenericError(val message: String) : ValidationError
    }

    sealed interface NetworkError : AppError {
        object NoInternet : NetworkError
        object ServerError : NetworkError
        data class ApiError(val code: Int, val message: String) : NetworkError
    }

    data class UnexpectedError(val throwable: Throwable) : AppError
}
