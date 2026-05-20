package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.UserId

interface RemoveMember {
    suspend operator fun invoke(courseId: CourseId, userId: UserId): DomainResult<Unit>
}
