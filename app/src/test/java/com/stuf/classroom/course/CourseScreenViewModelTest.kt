package com.stuf.classroom.course

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.UserId
import com.stuf.domain.usecase.ChangeMemberRole
import com.stuf.domain.usecase.GetCourseInfo
import com.stuf.domain.usecase.GetCourseMembers
import com.stuf.domain.usecase.GetCourseFeed
import com.stuf.domain.usecase.LeaveCourse
import com.stuf.domain.usecase.RemoveMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

/**
 * TDD-спека для CourseScreenViewModel.
 *
 * Ожидаемый контракт:
 * - uiState с данными курса, фидом постов и участниками;
 * - начальная загрузка при старте;
 * - действия по участникам и выходу из курса.
 *
 * Реализация CourseScreenViewModel должна удовлетворять этим тестам.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CourseScreenViewModelTest {

    private class FakeGetCourseInfo : GetCourseInfo {
        var lastCourseId: CourseId? = null
        var result: DomainResult<Course> = DomainResult.Failure(DomainError.Unknown())

        override suspend fun invoke(courseId: CourseId): DomainResult<Course> {
            lastCourseId = courseId
            return result
        }
    }

    private class FakeGetCourseFeed : GetCourseFeed {
        var lastArgs: Triple<CourseId, Int, Int>? = null
        var result: DomainResult<List<Post>> = DomainResult.Success(emptyList())

        override suspend fun invoke(
            courseId: CourseId,
            skip: Int,
            take: Int,
        ): DomainResult<List<Post>> {
            lastArgs = Triple(courseId, skip, take)
            return result
        }
    }

    private class FakeGetCourseMembers : GetCourseMembers {
        var lastCourseId: CourseId? = null
        var lastQuery: String? = null
        var result: DomainResult<List<CourseMember>> = DomainResult.Success(emptyList())

        override suspend fun invoke(courseId: CourseId, query: String?): DomainResult<List<CourseMember>> {
            lastCourseId = courseId
            lastQuery = query
            return result
        }
    }

    private class FakeChangeMemberRole : ChangeMemberRole {
        var lastCourseId: CourseId? = null
        var lastUserId: UserId? = null
        var lastRole: CourseRole? = null
        var result: DomainResult<Unit> = DomainResult.Success(Unit)

        override suspend fun invoke(
            courseId: CourseId,
            userId: UserId,
            newRole: CourseRole,
        ): DomainResult<Unit> {
            lastCourseId = courseId
            lastUserId = userId
            lastRole = newRole
            return result
        }
    }

    private class FakeRemoveMember : RemoveMember {
        var lastCourseId: CourseId? = null
        var lastUserId: UserId? = null
        var result: DomainResult<Unit> = DomainResult.Success(Unit)

        override suspend fun invoke(courseId: CourseId, userId: UserId): DomainResult<Unit> {
            lastCourseId = courseId
            lastUserId = userId
            return result
        }
    }

    private class FakeLeaveCourse : LeaveCourse {
        var lastCourseId: CourseId? = null
        var result: DomainResult<Unit> = DomainResult.Success(Unit)

        override suspend fun invoke(courseId: CourseId): DomainResult<Unit> {
            lastCourseId = courseId
            return result
        }
    }

    private lateinit var fakeGetCourseInfo: FakeGetCourseInfo
    private lateinit var fakeGetCourseFeed: FakeGetCourseFeed
    private lateinit var fakeGetCourseMembers: FakeGetCourseMembers
    private lateinit var fakeChangeMemberRole: FakeChangeMemberRole
    private lateinit var fakeRemoveMember: FakeRemoveMember
    private lateinit var fakeLeaveCourse: FakeLeaveCourse

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CourseScreenViewModel

    private val courseId: CourseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000100"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGetCourseInfo = FakeGetCourseInfo()
        fakeGetCourseFeed = FakeGetCourseFeed()
        fakeGetCourseMembers = FakeGetCourseMembers()
        fakeChangeMemberRole = FakeChangeMemberRole()
        fakeRemoveMember = FakeRemoveMember()
        fakeLeaveCourse = FakeLeaveCourse()

        viewModel = CourseScreenViewModel(
            courseId = courseId,
            getCourseInfo = fakeGetCourseInfo,
            getCourseFeed = fakeGetCourseFeed,
            getCourseMembers = fakeGetCourseMembers,
            changeMemberRole = fakeChangeMemberRole,
            removeMember = fakeRemoveMember,
            leaveCourse = fakeLeaveCourse,
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load success fills course posts and members`() = runTest(testDispatcher) {
        val course = Course(
            id = courseId,
            title = "Course Title",
            inviteCode = "INV123",
        )
        fakeGetCourseInfo.result = DomainResult.Success(course)

        val postId = PostId(UUID.fromString("00000000-0000-0000-0000-000000000102"))
        val post = Post(
            id = postId,
            courseId = courseId,
            kind = PostKind.ANNOUNCEMENT,
            title = "Post 1",
            text = "Text",
            createdAt = OffsetDateTime.now(),
        )
        fakeGetCourseFeed.result = DomainResult.Success(listOf(post))

        val memberId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000103"))
        val member = CourseMember(
            id = memberId,
            credentials = "Teacher Name",
            email = "teacher@example.com",
            role = CourseRole.TEACHER,
        )
        fakeGetCourseMembers.result = DomainResult.Success(listOf(member))

        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingCourse)
        assertFalse(state.isLoadingFeed)
        assertFalse(state.isLoadingMembers)
        assertNull(state.error)

        assertEquals("Course Title", state.courseTitle)
        assertEquals("INV123", state.inviteCode)
        assertEquals(1, state.posts.size)
        assertEquals(postId, state.posts[0].id)

        assertEquals(1, state.members.size)
        assertEquals(memberId, state.members[0].id)
    }

    @Test
    fun `initial load failure sets error and clears loading`() = runTest(testDispatcher) {
        fakeGetCourseInfo.result = DomainResult.Failure(DomainError.Unknown())
        fakeGetCourseFeed.result = DomainResult.Failure(DomainError.Unknown())
        fakeGetCourseMembers.result = DomainResult.Failure(DomainError.Unknown())

        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingCourse)
        assertFalse(state.isLoadingFeed)
        assertFalse(state.isLoadingMembers)
        assertNotNull(state.error)
    }

    @Test
    fun `removeMember success removes member from state and calls use case`() = runTest(testDispatcher) {
        val memberId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000200"))
        val member = CourseMember(
            id = memberId,
            credentials = "Student Name",
            email = "student@example.com",
            role = CourseRole.STUDENT,
        )
        fakeGetCourseInfo.result = DomainResult.Success(
            Course(
                id = courseId,
                title = "Course",
                inviteCode = "INV",
            ),
        )
        fakeGetCourseFeed.result = DomainResult.Success(emptyList())
        fakeGetCourseMembers.result = DomainResult.Success(listOf(member))

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onRemoveMemberClick(memberId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.members.isEmpty())
        assertEquals(courseId, fakeRemoveMember.lastCourseId)
        assertEquals(memberId, fakeRemoveMember.lastUserId)
    }

    @Test
    fun `changeMemberRole toggles between student and teacher and updates state`() = runTest(testDispatcher) {
        val memberId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000300"))
        val member = CourseMember(
            id = memberId,
            credentials = "Student Name",
            email = "student@example.com",
            role = CourseRole.STUDENT,
        )
        fakeGetCourseInfo.result = DomainResult.Success(
            Course(
                id = courseId,
                title = "Course",
                inviteCode = "INV",
            ),
        )
        fakeGetCourseFeed.result = DomainResult.Success(emptyList())
        fakeGetCourseMembers.result = DomainResult.Success(listOf(member))

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onChangeMemberRoleClick(memberId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CourseRole.TEACHER, state.members.single().role)
        assertEquals(courseId, fakeChangeMemberRole.lastCourseId)
        assertEquals(memberId, fakeChangeMemberRole.lastUserId)
        assertEquals(CourseRole.TEACHER, fakeChangeMemberRole.lastRole)
    }

    @Test
    fun `leaveCourse calls use case`() = runTest(testDispatcher) {
        val course = Course(
            id = courseId,
            title = "Course",
            inviteCode = "INV",
        )
        fakeGetCourseInfo.result = DomainResult.Success(course)
        fakeGetCourseFeed.result = DomainResult.Success(emptyList())
        fakeGetCourseMembers.result = DomainResult.Success(emptyList())

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onLeaveCourseClick()
        advanceUntilIdle()

        assertEquals(courseId, fakeLeaveCourse.lastCourseId)
    }

    @Test
    fun `leaveCourse failure sets error`() = runTest(testDispatcher) {
        val course = Course(
            id = courseId,
            title = "Course",
            inviteCode = "INV",
        )
        fakeGetCourseInfo.result = DomainResult.Success(course)
        fakeGetCourseFeed.result = DomainResult.Success(emptyList())
        fakeGetCourseMembers.result = DomainResult.Success(emptyList())
        fakeLeaveCourse.result = DomainResult.Failure(DomainError.Unknown())

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onLeaveCourseClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
    }
}

