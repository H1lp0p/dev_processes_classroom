package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId

interface CreateCourse {
    suspend operator fun invoke(title: String): DomainResult<Course>
}

interface JoinCourse {
    suspend operator fun invoke(inviteCode: String): DomainResult<Course>
}

interface GetCourseMembers {
    suspend operator fun invoke(courseId: CourseId, query: String? = null): DomainResult<List<CourseMember>>
}

interface ChangeMemberRole {
    suspend operator fun invoke(courseId: CourseId, userId: UserId, newRole: CourseRole): DomainResult<Unit>
}

interface RemoveMember {
    suspend operator fun invoke(courseId: CourseId, userId: UserId): DomainResult<Unit>
}

interface LeaveCourse {
    suspend operator fun invoke(courseId: CourseId): DomainResult<Unit>
}
