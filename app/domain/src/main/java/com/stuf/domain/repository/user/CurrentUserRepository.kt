package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.User

interface CurrentUserRepository {
    suspend fun getCurrentUser(): DomainResult<User>
}
