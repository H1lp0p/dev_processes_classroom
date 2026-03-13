package com.stuf.classroom.auth

import com.stuf.domain.repository.AuthSession

sealed class AuthState {
    /** До первого вызова initialize(); показываем экран загрузки. */
    data object Initial : AuthState()
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val session: AuthSession) : AuthState()
}

