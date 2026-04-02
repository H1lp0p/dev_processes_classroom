package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId

interface ChangeMemberRole {
    suspend operator fun invoke(courseId: CourseId, userId: UserId, newRole: CourseRole): DomainResult<Unit>
}
