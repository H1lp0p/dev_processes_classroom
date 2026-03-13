package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import com.stuf.domain.usecase.impl.GetCourseMembersUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

private class FakeCourseMembersRepository : CourseRepository {
    var lastGetCourseMembersQuery: String? = null

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
    ): DomainResult<List<CourseMember>> {
        lastGetCourseMembersQuery = query
        return DomainResult.Success(emptyList())
    }

    override suspend fun changeMemberRole(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        error("Not needed in this fake")
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class GetCourseMembersUseCaseTest {

    @Test
    fun `query with spaces is trimmed`() {
        val repo = FakeCourseMembersRepository()
        val useCase : GetCourseMembers = GetCourseMembersUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        kotlinx.coroutines.runBlocking {
            useCase(courseId, "  john  ")
        }

        assertEquals("john", repo.lastGetCourseMembersQuery)
    }

    @Test
    fun `blank query is passed as null to repository`() {
        val repo = FakeCourseMembersRepository()
        val useCase : GetCourseMembers = GetCourseMembersUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        kotlinx.coroutines.runBlocking {
            useCase(courseId, "   ")
        }

        assertEquals(null, repo.lastGetCourseMembersQuery)
    }
}
