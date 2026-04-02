package com.stuf.data.demo

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.User
import com.stuf.domain.repository.CurrentUserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoCurrentUserRepository @Inject constructor() : CurrentUserRepository {

    override suspend fun getCurrentUser(): DomainResult<User> =
        DomainResult.Success(
            User(
                id = DemoIds.userStudent,
                credentials = "Студент Демо",
                email = "student@demo.local",
            ),
        )
}
