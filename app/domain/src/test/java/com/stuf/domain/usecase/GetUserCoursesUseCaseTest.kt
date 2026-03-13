package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.GetUserCoursesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeGetUserCoursesRepository : CourseRepository {
    var getUserCoursesResult: DomainResult<List<UserCourse>> = DomainResult.Success(emptyList())

    override suspend fun getUserCourses(): DomainResult<List<UserCourse>> = getUserCoursesResult

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
        newRole: CourseRole,
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

class GetUserCoursesUseCaseTest {

    @Test
    fun `success returns list from repository unchanged`() {
        val id1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val id2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val userCourse1 = UserCourse(
            id = CourseId(id1),
            title = "Course 1",
            role = CourseRole.TEACHER,
        )
        val userCourse2 = UserCourse(
            id = CourseId(id2),
            title = "Course 2",
            role = CourseRole.STUDENT,
        )
        val repo = FakeGetUserCoursesRepository().apply {
            getUserCoursesResult = DomainResult.Success(listOf(userCourse1, userCourse2))
        }
        val useCase: GetUserCourses = GetUserCoursesUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase()
        }

        assertTrue(result is DomainResult.Success<*>)
        val list = (result as DomainResult.Success<List<UserCourse>>).value
        assertEquals(2, list.size)
        assertEquals(id1, list[0].id.value)
        assertEquals("Course 1", list[0].title)
        assertEquals(CourseRole.TEACHER, list[0].role)
        assertEquals(id2, list[1].id.value)
        assertEquals("Course 2", list[1].title)
        assertEquals(CourseRole.STUDENT, list[1].role)
    }

    @Test
    fun `failure from repository is propagated`() {
        val repo = FakeGetUserCoursesRepository().apply {
            getUserCoursesResult = DomainResult.Failure(DomainError.Unauthorized)
        }
        val useCase: GetUserCourses = GetUserCoursesUseCase(repo)

        val result = kotlinx.coroutines.runBlocking {
            useCase()
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }
}
