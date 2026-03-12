package com.stuf.domain.common

sealed class DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()
}

sealed class DomainError {
    object NotFound : DomainError()
    object Unauthorized : DomainError()
    object Forbidden : DomainError()
    data class Validation(val message: String) : DomainError()
    data class Network(val cause: Throwable? = null) : DomainError()
    data class Unknown(val cause: Throwable? = null) : DomainError()
}

