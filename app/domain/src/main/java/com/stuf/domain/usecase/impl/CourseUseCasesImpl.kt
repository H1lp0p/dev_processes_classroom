package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.ChangeMemberRole
import com.stuf.domain.usecase.CreateCourse
import com.stuf.domain.usecase.GetCourseMembers
import com.stuf.domain.usecase.JoinCourse
import com.stuf.domain.usecase.LeaveCourse
import com.stuf.domain.usecase.RemoveMember

class CreateCourseUseCase(
    private val repository: CourseRepository,
) : CreateCourse {

    override suspend fun invoke(title: String): DomainResult<Course> {
        val trimmed = title.trim()
        if (trimmed.isBlank()) {
            return DomainResult.Failure(DomainError.Validation("Title must not be blank"))
        }
        return repository.createCourse(trimmed)
    }
}

class JoinCourseUseCase(
    private val repository: CourseRepository,
) : JoinCourse {

    override suspend fun invoke(inviteCode: String): DomainResult<Course> {
        val trimmedInviteCode = inviteCode.trim().ifBlank {
            return DomainResult.Failure(DomainError.Validation("inviteCode can't be empty"))
        }
        return repository.joinCourse(trimmedInviteCode)
    }
}

class GetCourseMembersUseCase(
    private val repository: CourseRepository,
) : GetCourseMembers {

    override suspend fun invoke(
        courseId: CourseId,
        query: String?,
    ): DomainResult<List<CourseMember>> {

        val trimmedQuery = query?.let {
            it.trim().ifBlank { null }
        }

        return repository.getCourseMembers(courseId, trimmedQuery)
    }
}

class ChangeMemberRoleUseCase(
    private val repository: CourseRepository,
) : ChangeMemberRole {

    override suspend fun invoke(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): DomainResult<Unit> {
        return repository.changeMemberRole(courseId, userId, newRole)
    }
}

class RemoveMemberUseCase(
    private val repository: CourseRepository,
) : RemoveMember {

    override suspend fun invoke(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        return repository.removeMember(courseId, userId)
    }
}

class LeaveCourseUseCase(
    private val repository: CourseRepository,
) : LeaveCourse {

    override suspend fun invoke(courseId: CourseId): DomainResult<Unit> {
        return repository.leaveCourse(courseId)
    }
}

