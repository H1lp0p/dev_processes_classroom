package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course

interface JoinCourse {
    suspend operator fun invoke(inviteCode: String): DomainResult<Course>
}
