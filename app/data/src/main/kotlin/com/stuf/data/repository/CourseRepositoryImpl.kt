package com.stuf.data.repository

import android.util.Log
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
import com.stuf.domain.model.UserCourse
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val api: CourseApi,
) : CourseRepository {

    override suspend fun getUserCourses(): DomainResult<List<UserCourse>> {
        val response = safeCall { api.apiUserCoursesGet() }
        return when (response) {
            is DomainResult.Success -> DomainResult.Success(
                response.value.data?.records.orEmpty().map { it.toUserCourseDomain() },
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
            Log.e("CourseRepository", "Network error on request", e)
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            Log.e("CourseRepository", "Unexpected error on request", e)
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        // Логируем URL, Authorization и код ответа
        try {
            val rawRequest = response.raw().request
            val url = rawRequest.url.toString()
            val authHeader = rawRequest.header("Authorization")
            Log.d(
                "CourseRepository",
                "HTTP ${response.code()} ${if (response.isSuccessful) "OK" else "ERROR"} " +
                    "url=$url, Authorization=$authHeader",
            )

            if (!response.isSuccessful) {
                val errorBody = try {
                    response.errorBody()?.string() ?: "(no error body)"
                } catch (e: IOException) {
                    "(failed to read error body: ${e.message})"
                }
                Log.d("CourseRepository", "Error body: $errorBody")
                return DomainResult.Failure(httpCodeToDomainError(response.code()))
            }
        } catch (e: Exception) {
            Log.w("CourseRepository", "Failed to log HTTP request/response", e)
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        return DomainResult.Success(body)
    }

    private fun UserCourseDto.toUserCourseDomain(): UserCourse =
        UserCourse(
            id = CourseId(requireNotNull(id)),
            title = title ?: "",
            role = when (role) {
                UserRoleType.student -> CourseRole.STUDENT
                UserRoleType.teacher -> CourseRole.TEACHER
                null -> CourseRole.STUDENT
            },
        )

    private fun CourseDetailsDto.toDomain(): Course =
        Course(
            id = CourseId(id),
            title = title,
            inviteCode = inviteCode,
            authorId = UserId(authorId),
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

