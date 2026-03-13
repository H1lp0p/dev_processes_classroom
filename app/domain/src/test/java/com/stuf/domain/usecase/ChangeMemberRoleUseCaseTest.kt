package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.ChangeMemberRoleUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeChangeMemberRoleRepository : CourseRepository {
    var lastChangeRoleArgs: Triple<CourseId, UserId, CourseRole>? = null

    override suspend fun getUserCourses(): DomainResult<List<com.stuf.domain.model.UserCourse>> =
        DomainResult.Success(emptyList())

    override suspend fun createCourse(title: String): DomainResult<com.stuf.domain.model.Course> {
        error("Not needed in this fake")
    }

    override suspend fun joinCourse(inviteCode: String): DomainResult<com.stuf.domain.model.Course> {
        error("Not needed in this fake")
    }

    override suspend fun getCourseInfo(courseId: CourseId): DomainResult<com.stuf.domain.model.Course> {
        error("Not needed in this fake")
    }

    override suspend fun getCourseMembers(
        courseId: CourseId,
        query: String?,
    ): DomainResult<List<com.stuf.domain.model.CourseMember>> {
        error("Not needed in this fake")
    }

    override suspend fun changeMemberRole(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): DomainResult<Unit> {
        lastChangeRoleArgs = Triple(courseId, userId, newRole)
        return DomainResult.Success(Unit)
    }

    override suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class ChangeMemberRoleUseCaseTest {

    @Test
    fun `valid args delegates to repository and returns success`() {
        val repo = FakeChangeMemberRoleRepository()
        val useCase : ChangeMemberRole = ChangeMemberRoleUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val newRole = CourseRole.TEACHER

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, userId, newRole)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(courseId, repo.lastChangeRoleArgs?.first)
        assertEquals(userId, repo.lastChangeRoleArgs?.second)
        assertEquals(newRole, repo.lastChangeRoleArgs?.third)
    }
}
