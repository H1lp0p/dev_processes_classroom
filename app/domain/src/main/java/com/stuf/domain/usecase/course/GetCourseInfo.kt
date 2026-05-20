package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId

interface GetCourseInfo {
    suspend operator fun invoke(courseId: CourseId): DomainResult<Course>
}
