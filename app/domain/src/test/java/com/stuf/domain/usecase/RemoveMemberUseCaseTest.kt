package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.RemoveMemberUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeRemoveMemberRepository : CourseRepository {
    var lastRemoveMemberArgs: Pair<CourseId, UserId>? = null

    override suspend fun getUserCourses(): DomainResult<List<com.stuf.domain.model.Course>> =
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
        newRole: com.stuf.domain.model.CourseRole,
    ): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        lastRemoveMemberArgs = Pair(courseId, userId)
        return DomainResult.Success(Unit)
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class RemoveMemberUseCaseTest {

    @Test
    fun `delegates to repository and returns success`() {
        val repo = FakeRemoveMemberRepository()
        val useCase : RemoveMember = RemoveMemberUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, userId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(courseId, repo.lastRemoveMemberArgs?.first)
        assertEquals(userId, repo.lastRemoveMemberArgs?.second)
    }
}
