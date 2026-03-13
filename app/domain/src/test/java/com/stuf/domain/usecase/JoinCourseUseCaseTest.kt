package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.JoinCourseUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeJoinCourseRepository : CourseRepository {
    var lastJoinCourseCode: String? = null

    override suspend fun getUserCourses(): DomainResult<List<com.stuf.domain.model.UserCourse>> =
        DomainResult.Success(emptyList())

    override suspend fun createCourse(title: String): DomainResult<Course> {
        error("Not needed in this fake")
    }

    override suspend fun joinCourse(inviteCode: String): DomainResult<Course> {
        lastJoinCourseCode = inviteCode
        return DomainResult.Success(
            Course(
                id = CourseId(UUID.randomUUID()),
                title = "Joined",
                inviteCode = inviteCode,
            ),
        )
    }

    override suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course> {
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
        error("Not needed in this fake")
    }
}

class JoinCourseUseCaseTest {

    @Test
    fun `valid invite code is trimmed and passed to repository`() {
        val repo = FakeJoinCourseRepository()
        val useCase : JoinCourse = JoinCourseUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase("  ABC123  ")
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals("ABC123", repo.lastJoinCourseCode)
        val course = (result as DomainResult.Success<Course>).value
        assertEquals("ABC123", course.inviteCode)
    }

    @Test
    fun `blank invite code returns validation error and does not call repository`() {
        val repo = FakeJoinCourseRepository()
        val useCase : JoinCourse = JoinCourseUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase("   ")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastJoinCourseCode)
    }
}
