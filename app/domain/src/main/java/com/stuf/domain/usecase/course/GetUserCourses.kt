package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.UserCourse

interface GetUserCourses {
    suspend operator fun invoke(): DomainResult<List<UserCourse>>
}
