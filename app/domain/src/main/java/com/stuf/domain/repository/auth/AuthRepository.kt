package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult

interface AuthRepository {
    suspend fun login(email: String, password: String): DomainResult<AuthSession>
    suspend fun register(credentials: String, email: String, password: String): DomainResult<AuthSession>
    suspend fun refresh(): DomainResult<AuthSession>
    suspend fun changePassword(oldPassword: String, newPassword: String): DomainResult<Unit>
}
