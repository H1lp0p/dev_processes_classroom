package com.stuf.data.repository

import com.stuf.data.api.CourseApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ChangeRoleRequestDto
import com.stuf.data.model.CourseDetailsDto
import com.stuf.data.model.CourseMemberDto
import com.stuf.data.model.CreateUpdateCourseRequestDto
import com.stuf.data.model.JoinCourseRequestDto
import com.stuf.data.model.UserCourseDto
import com.stuf.data.model.UserRoleType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import retrofit2.Response
import java.io.IOException

class CourseRepositoryImpl(
    private val api: CourseApi,
) : CourseRepository {

    override suspend fun getUserCourses(): DomainResult<List<Course>> {
        val response = safeCall { api.apiUserCoursesGet() }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(
                response.value.data?.records.orEmpty().map { it.toDomain() },
            )
            is DomainResult.Failure -> response
        }
    }

    override suspend fun createCourse(title: String): DomainResult<Course> {
        val dto = CreateUpdateCourseRequestDto(title = title)
        val response = safeCall { api.apiCoursePost(dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                DomainResult.Success(
                    Course(
                        id = CourseId(data.id),
                        title = data.title,
                        inviteCode = null,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun joinCourse(inviteCode: String): DomainResult<Course> {
        val dto = JoinCourseRequestDto(inviteCode = inviteCode)
        val response = safeCall { api.apiCourseJoinPost(dto) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                DomainResult.Success(
                    Course(
                        id = CourseId(data.id),
                        title = data.title,
                        inviteCode = null,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getCourseInfo(courseId: CourseId): DomainResult<Course> {
        val response = safeCall { api.apiCourseIdGet(courseId.value) }
        return when (response) {
            is DomainResult.Success -> {
                val data = response.value.data
                    ?: return DomainResult.Failure(DomainError.Unknown())
                DomainResult.Success(data.toDomain())
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun getCourseMembers(
        courseId: CourseId,
        query: String?,
    ): DomainResult<List<CourseMember>> {
        val response = safeCall { api.apiCourseIdMembersGet(courseId.value, query = query) }
        return when (response) {
            is DomainResult.Success -> {
                val records = response.value.data?.records.orEmpty()
                DomainResult.Success(records.map { it.toDomain() })
            }
            is DomainResult.Failure -> response
        }
    }

    override suspend fun changeMemberRole(
        courseId: CourseId,
        userId: UserId,
        newRole: CourseRole,
    ): DomainResult<Unit> {
        val dto = ChangeRoleRequestDto(
            role = when (newRole) {
                CourseRole.STUDENT -> UserRoleType.student
                CourseRole.TEACHER -> UserRoleType.teacher
            },
        )
        val response = safeCall { api.apiCourseIdMembersUserIdRolePut(courseId.value, userId.value, dto) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun removeMember(courseId: CourseId, userId: UserId): DomainResult<Unit> {
        val response = safeCall { api.apiCourseIdMembersUserIdDelete(courseId.value, userId.value) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
    }

    override suspend fun leaveCourse(courseId: CourseId): DomainResult<Unit> {
        val response = safeCall { api.apiCourseIdLeaveDelete(courseId.value) }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(Unit)
            is DomainResult.Failure -> response
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): DomainResult<T> {
        val response = try {
            block()
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        return DomainResult.Success(body)
    }

    private fun UserCourseDto.toDomain(): Course =
        Course(
            id = CourseId(requireNotNull(id)),
            title = title ?: "",
            inviteCode = null,
        )

    private fun CourseDetailsDto.toDomain(): Course =
        Course(
            id = CourseId(id),
            title = title,
            inviteCode = inviteCode,
        )

    private fun CourseMemberDto.toDomain(): CourseMember =
        CourseMember(
            id = UserId(id),
            credentials = credentials,
            email = email,
            role = when (role) {
                UserRoleType.student -> CourseRole.STUDENT
                UserRoleType.teacher -> CourseRole.TEACHER
            },
        )
}

