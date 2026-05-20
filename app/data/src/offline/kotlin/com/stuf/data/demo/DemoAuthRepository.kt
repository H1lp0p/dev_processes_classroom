package com.stuf.data.demo

import com.stuf.data.auth.AuthSessionStorage
import com.stuf.data.auth.AuthTokenManager
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoAuthRepository @Inject constructor(
    private val authSessionStorage: AuthSessionStorage,
    private val authTokenManager: AuthTokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): DomainResult<AuthSession> {
        val session = AuthSession(accessToken = "demo-access", refreshToken = "demo-refresh")
        authSessionStorage.saveSession(session)
        authTokenManager.applySession(session)
        return DomainResult.Success(session)
    }

    override suspend fun register(
        credentials: String,
        email: String,
        password: String,
    ): DomainResult<AuthSession> {
        val session = AuthSession(accessToken = "demo-access", refreshToken = "demo-refresh")
        authSessionStorage.saveSession(session)
        authTokenManager.applySession(session)
        return DomainResult.Success(session)
    }

    override suspend fun refresh(): DomainResult<AuthSession> {
        val stored = authSessionStorage.sessionFlow.first()
            ?: return DomainResult.Failure(DomainError.Unauthorized)
        authTokenManager.applySession(stored)
        return DomainResult.Success(stored)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): DomainResult<Unit> {
        if (oldPassword.isBlank() || newPassword.length < 6) {
            return DomainResult.Failure(DomainError.Validation("Проверьте старый и новый пароль"))
        }
        return DomainResult.Success(Unit)
    }
}
