package com.stuf.data.repository

import com.stuf.data.api.CourseApi
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.ChangeRoleRequestDto
import com.stuf.data.model.ChangeRoleResponseDto
import com.stuf.data.model.ChangeRoleResponseDtoApiResponse
import com.stuf.data.model.CourseDetailsDto
import com.stuf.data.model.CourseDetailsDtoApiResponse
import com.stuf.data.model.CourseMemberDto
import com.stuf.data.model.CourseMemberDtoPagedResponse
import com.stuf.data.model.CourseMemberDtoPagedResponseApiResponse
import com.stuf.data.model.CreateUpdateCourseRequestDto
import com.stuf.data.model.CreateUpdateCourseResponseDto
import com.stuf.data.model.CreateUpdateCourseResponseDtoApiResponse
import com.stuf.data.model.JoinCourseRequestDto
import com.stuf.data.model.JoinCourseResponseDto
import com.stuf.data.model.JoinCourseResponseDtoApiResponse
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserCourseDto
import com.stuf.data.model.UserCourseDtoPagedResponse
import com.stuf.data.model.UserCourseDtoPagedResponseApiResponse
import com.stuf.data.model.UserRoleType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CourseRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.UUID

private class FakeCourseApi : CourseApi {
    var lastUserCoursesSkip: Int? = null
    var lastUserCoursesTake: Int? = null
    var lastCreateTitle: String? = null
    var lastJoinInviteCode: String? = null
    var lastCourseIdForInfo: UUID? = null
    var lastMembersCourseId: UUID? = null
    var lastMembersQuery: String? = null
    var lastChangeRoleCourseId: UUID? = null
    var lastChangeRoleUserId: UUID? = null
    var lastChangeRole: UserRoleType? = null
    var lastRemoveMemberCourseId: UUID? = null
    var lastRemoveMemberUserId: UUID? = null
    var lastLeaveCourseId: UUID? = null

    var userCoursesResponse: Response<UserCourseDtoPagedResponseApiResponse>? = null
    var createCourseResponse: Response<CreateUpdateCourseResponseDtoApiResponse>? = null
    var joinCourseResponse: Response<JoinCourseResponseDtoApiResponse>? = null
    var courseInfoResponse: Response<CourseDetailsDtoApiResponse>? = null
    var membersResponse: Response<CourseMemberDtoPagedResponseApiResponse>? = null
    var changeRoleResponse: Response<ChangeRoleResponseDtoApiResponse>? = null
    var removeMemberResponse: Response<ObjectApiResponse>? = null
    var leaveCourseResponse: Response<ObjectApiResponse>? = null

    override suspend fun apiCourseIdGet(id: UUID): Response<CourseDetailsDtoApiResponse> {
        lastCourseIdForInfo = id
        return requireNotNull(courseInfoResponse)
    }

    override suspend fun apiCourseIdLeaveDelete(id: UUID): Response<ObjectApiResponse> {
        lastLeaveCourseId = id
        return requireNotNull(leaveCourseResponse)
    }

    override suspend fun apiCourseIdMembersGet(
        id: UUID,
        skip: Int?,
        take: Int?,
        query: String?,
    ): Response<CourseMemberDtoPagedResponseApiResponse> {
        lastMembersCourseId = id
        lastMembersQuery = query
        return requireNotNull(membersResponse)
    }

    override suspend fun apiCourseIdMembersUserIdDelete(id: UUID, userId: UUID): Response<ObjectApiResponse> {
        lastRemoveMemberCourseId = id
        lastRemoveMemberUserId = userId
        return requireNotNull(removeMemberResponse)
    }

    override suspend fun apiCourseIdMembersUserIdRolePut(
        id: UUID,
        userId: UUID,
        changeRoleRequestDto: ChangeRoleRequestDto?,
    ): Response<ChangeRoleResponseDtoApiResponse> {
        lastChangeRoleCourseId = id
        lastChangeRoleUserId = userId
        lastChangeRole = changeRoleRequestDto?.role
        return requireNotNull(changeRoleResponse)
    }

    override suspend fun apiCourseIdPut(
        id: UUID,
        createUpdateCourseRequestDto: CreateUpdateCourseRequestDto?,
    ): Response<CreateUpdateCourseResponseDtoApiResponse> {
        // не используется в текущем CourseRepository
        error("Not needed in these tests")
    }

    override suspend fun apiCourseJoinPost(
        joinCourseRequestDto: JoinCourseRequestDto?,
    ): Response<JoinCourseResponseDtoApiResponse> {
        lastJoinInviteCode = joinCourseRequestDto?.inviteCode
        return requireNotNull(joinCourseResponse)
    }

    override suspend fun apiCoursePost(
        createUpdateCourseRequestDto: CreateUpdateCourseRequestDto?,
    ): Response<CreateUpdateCourseResponseDtoApiResponse> {
        lastCreateTitle = createUpdateCourseRequestDto?.title
        return requireNotNull(createCourseResponse)
    }

    override suspend fun apiUserCoursesGet(skip: Int?, take: Int?): Response<UserCourseDtoPagedResponseApiResponse> {
        lastUserCoursesSkip = skip
        lastUserCoursesTake = take
        return requireNotNull(userCoursesResponse)
    }
}

class CourseRepositoryImplTest {

    @Test
    fun `getUserCourses success maps list of courses`() {
        val api = FakeCourseApi().apply {
            userCoursesResponse = Response.success(
                UserCourseDtoPagedResponseApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = UserCourseDtoPagedResponse(
                        records = listOf(
                            UserCourseDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                title = "Course 1",
                                role = UserRoleType.teacher,
                            ),
                            UserCourseDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                                title = "Course 2",
                                role = UserRoleType.student,
                            ),
                        ),
                        totalRecords = 2,
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.getUserCourses()
        }

        assertTrue(result is DomainResult.Success<List<Course>>)
        val courses = (result as DomainResult.Success<List<Course>>).value
        assertEquals(2, courses.size)
        assertEquals("Course 1", courses[0].title)
        assertEquals("Course 2", courses[1].title)
    }

    @Test
    fun `getUserCourses http 401 returns Unauthorized`() {
        val api = FakeCourseApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            userCoursesResponse = Response.error(401, body)
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.getUserCourses()
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }

    @Test
    fun `createCourse success maps created course`() {
        val api = FakeCourseApi().apply {
            createCourseResponse = Response.success(
                CreateUpdateCourseResponseDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = CreateUpdateCourseResponseDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                        title = "New Course",
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.createCourse("New Course")
        }

        assertTrue(result is DomainResult.Success<Course>)
        val course = (result as DomainResult.Success<Course>).value
        assertEquals("New Course", course.title)
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000010"), course.id.value)
        assertEquals("New Course", api.lastCreateTitle)
    }

    @Test
    fun `joinCourse success maps joined course`() {
        val api = FakeCourseApi().apply {
            joinCourseResponse = Response.success(
                JoinCourseResponseDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = JoinCourseResponseDto(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000020"),
                        title = "Joined Course",
                        role = UserRoleType.student,
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.joinCourse(inviteCode = "INVITE123")
        }

        assertTrue(result is DomainResult.Success<Course>)
        val course = (result as DomainResult.Success<Course>).value
        assertEquals("Joined Course", course.title)
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000020"), course.id.value)
        assertEquals("INVITE123", api.lastJoinInviteCode)
    }

    @Test
    fun `getCourseInfo success maps course details`() {
        val courseId = UUID.fromString("00000000-0000-0000-0000-000000000030")
        val api = FakeCourseApi().apply {
            courseInfoResponse = Response.success(
                CourseDetailsDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = CourseDetailsDto(
                        id = courseId,
                        title = "Course Info",
                        role = UserRoleType.teacher,
                        authorId = UUID.randomUUID(),
                        inviteCode = "INV123",
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.getCourseInfo(CourseId(courseId))
        }

        assertTrue(result is DomainResult.Success<Course>)
        val course = (result as DomainResult.Success<Course>).value
        assertEquals("Course Info", course.title)
        assertEquals(courseId, course.id.value)
        assertEquals(courseId, api.lastCourseIdForInfo)
    }

    @Test
    fun `getCourseMembers success maps members list and passes query`() {
        val courseId = UUID.fromString("00000000-0000-0000-0000-000000000040")
        val api = FakeCourseApi().apply {
            membersResponse = Response.success(
                CourseMemberDtoPagedResponseApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = CourseMemberDtoPagedResponse(
                        records = listOf(
                            CourseMemberDto(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000041"),
                                credentials = "Teacher Name",
                                email = "teacher@example.com",
                                role = UserRoleType.teacher,
                            ),
                        ),
                        totalRecords = 1,
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.getCourseMembers(CourseId(courseId), query = "teach")
        }

        assertTrue(result is DomainResult.Success<List<CourseMember>>)
        val members = (result as DomainResult.Success<List<CourseMember>>).value
        assertEquals(1, members.size)
        assertEquals("Teacher Name", members[0].credentials)
        assertEquals("teach", api.lastMembersQuery)
        assertEquals(courseId, api.lastMembersCourseId)
    }

    @Test
    fun `changeMemberRole success returns Unit and sends correct role`() {
        val courseId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val api = FakeCourseApi().apply {
            changeRoleResponse = Response.success(
                ChangeRoleResponseDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = ChangeRoleResponseDto(
                        id = userId,
                        role = UserRoleType.teacher,
                    ),
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.changeMemberRole(
                courseId = CourseId(courseId),
                userId = UserId(userId),
                newRole = CourseRole.TEACHER,
            )
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(courseId, api.lastChangeRoleCourseId)
        assertEquals(userId, api.lastChangeRoleUserId)
        assertEquals(UserRoleType.teacher, api.lastChangeRole)
    }

    @Test
    fun `removeMember success returns Unit`() {
        val courseId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val api = FakeCourseApi().apply {
            removeMemberResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = null,
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.removeMember(
                courseId = CourseId(courseId),
                userId = UserId(userId),
            )
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(courseId, api.lastRemoveMemberCourseId)
        assertEquals(userId, api.lastRemoveMemberUserId)
    }

    @Test
    fun `leaveCourse success returns Unit`() {
        val courseId = UUID.randomUUID()
        val api = FakeCourseApi().apply {
            leaveCourseResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = null,
                ),
            )
        }
        val repository: CourseRepository = CourseRepositoryImpl(api)

        val result = runBlocking {
            repository.leaveCourse(CourseId(courseId))
        }

        assertTrue(result is DomainResult.Success<Unit>)
        assertEquals(courseId, api.lastLeaveCourseId)
    }
}

