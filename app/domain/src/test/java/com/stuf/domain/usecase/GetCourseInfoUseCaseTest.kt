package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.GetCourseInfoUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeGetCourseInfoRepository : CourseRepository {
    var lastCourseId: CourseId? = null
    val missingCourseId: CourseId = CourseId(UUID.randomUUID())

    private val sampleCourse: Course = Course(
        id = CourseId(UUID.randomUUID()),
        title = "Sample Course",
        inviteCode = "INV123",
        authorId = null,
    )

    override suspend fun getUserCourses(): DomainResult<List<com.stuf.domain.model.UserCourse>> {
        error("Not needed in this fake")
    }

    override suspend fun createCourse(title: String): DomainResult<com.stuf.domain.model.Course> {
        error("Not needed in this fake")
    }

    override suspend fun joinCourse(inviteCode: String): DomainResult<com.stuf.domain.model.Course> {
        error("Not needed in this fake")
    }

    override suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course> {
        if (courseId == missingCourseId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastCourseId = courseId
        return DomainResult.Success(
            sampleCourse.copy(id = courseId),
        )
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

class GetCourseInfoUseCaseTest {

    @Test
    fun `delegates to repository_and_returns_course`() {
        val repository: CourseRepository = FakeGetCourseInfoRepository()
        val useCase: GetCourseInfo = GetCourseInfoUseCase(repository)
        val courseId: CourseId = CourseId(UUID.randomUUID())

        val result: DomainResult<Course> = kotlinx.coroutines.runBlocking {
            useCase(courseId)
        }

        assertTrue(result is DomainResult.Success<*>)
        val success = result as DomainResult.Success<Course>
        assertEquals(courseId, (repository as FakeGetCourseInfoRepository).lastCourseId)
        assertEquals(courseId, success.value.id)
    }

    @Test
    fun `returns_not_found_error_when_repository_returns_NotFound`() {
        val repository: CourseRepository = FakeGetCourseInfoRepository()
        val useCase: GetCourseInfo = GetCourseInfoUseCase(repository)
        val missingId: CourseId = (repository as FakeGetCourseInfoRepository).missingCourseId

        val result: DomainResult<Course> = kotlinx.coroutines.runBlocking {
            useCase(missingId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}

