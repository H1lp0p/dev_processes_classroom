package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.repository.CourseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeCourseRepository : CourseRepository {
    var lastCreateCourseTitle: String? = null

    override suspend fun getUserCourses(): DomainResult<List<Course>> =
        DomainResult.Success(emptyList())

    override suspend fun createCourse(title: String): DomainResult<Course> {
        lastCreateCourseTitle = title
        return DomainResult.Success(
            Course(
                id = CourseId(UUID.randomUUID()),
                title = title,
                inviteCode = null,
            ),
        )
    }

    override suspend fun joinCourse(inviteCode: String): DomainResult<Course> {
        error("Not needed in this fake")
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

class CreateCourseUseCaseTest {

    @Test
    fun `valid title with spaces is trimmed and passed to repository`() {
        val repo = FakeCourseRepository()
        val useCase : CreateCourse = CreateCourseUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase("  Kotlin course  ")
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals("Kotlin course", repo.lastCreateCourseTitle)
        val course = (result as DomainResult.Success<Course>).value
        assertEquals("Kotlin course", course.title)
    }

    @Test
    fun `blank title returns validation error and does not call repository`() {
        val repo = FakeCourseRepository()
        val useCase : CreateCourse = CreateCourseUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase("   ")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals(null, repo.lastCreateCourseTitle)
    }
}

