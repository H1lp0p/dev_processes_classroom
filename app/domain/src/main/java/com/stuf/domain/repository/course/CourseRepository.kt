package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import com.stuf.domain.model.UserId

interface CourseRepository {
    suspend fun getUserCourses(): DomainResult<List<UserCourse>>
    suspend fun createCourse(title: String): DomainResult<Course>
    suspend fun joinCourse(inviteCode: String): DomainResult<Course>
    suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course>
    suspend fun getCourseMembers(
        courseId: CourseId,
        query: String? = null,
    ): DomainResult<List<CourseMember>>

    suspend fun changeMemberRole(courseId: CourseId, userId: UserId, newRole: CourseRole): DomainResult<Unit>
    suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit>
    suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit>
}
