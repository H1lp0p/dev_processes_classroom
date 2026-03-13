package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.LeaveCourseUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeLeaveCourseRepository : CourseRepository {
    var lastLeaveCourseId: CourseId? = null

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
        userId: com.stuf.domain.model.UserId,
        newRole: com.stuf.domain.model.CourseRole,
    ): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun removeMember(
        courseId: CourseId,
        userId: com.stuf.domain.model.UserId,
    ): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        lastLeaveCourseId = courseId
        return DomainResult.Success(Unit)
    }
}

class LeaveCourseUseCaseTest {

    @Test
    fun `delegates to repository and returns success`() {
        val repo = FakeLeaveCourseRepository()
        val useCase : LeaveCourse = LeaveCourseUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(courseId, repo.lastLeaveCourseId)
    }
}
