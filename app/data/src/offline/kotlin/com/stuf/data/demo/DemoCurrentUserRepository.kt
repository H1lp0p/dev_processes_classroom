package com.stuf.data.demo

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.User
import com.stuf.domain.repository.CurrentUserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoCurrentUserRepository @Inject constructor() : CurrentUserRepository {
    private var currentUser: User =
        User(
            id = DemoIds.userStudent,
            credentials = "Студент Демо",
            email = "student@demo.local",
        )

    override suspend fun getCurrentUser(): DomainResult<User> =
        DomainResult.Success(currentUser)

    override suspend fun updateCurrentUser(credentials: String, email: String): DomainResult<Unit> {
        if (credentials.isBlank()) {
            return DomainResult.Failure(com.stuf.domain.common.DomainError.Validation("Введите имя"))
        }
        if (email.isBlank() || !email.contains("@")) {
            return DomainResult.Failure(com.stuf.domain.common.DomainError.Validation("Введите корректный email"))
        }
        currentUser =
            currentUser.copy(
                credentials = credentials.trim(),
                email = email.trim(),
            )
        return DomainResult.Success(Unit)
    }
}
