package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember

interface GetCourseMembers {
    suspend operator fun invoke(courseId: CourseId, query: String? = null): DomainResult<List<CourseMember>>
}
