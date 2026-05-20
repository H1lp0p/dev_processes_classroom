package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId

interface LeaveCourse {
    suspend operator fun invoke(courseId: CourseId): DomainResult<Unit>
}
