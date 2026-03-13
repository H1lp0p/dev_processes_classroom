package com.stuf.classroom.courses

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import com.stuf.domain.usecase.GetUserCourses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * TDD-тесты для UserCoursesViewModel.
 * Ожидаемый контракт: uiState (courses, isLoading, error), загрузка при старте, onRetry.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserCoursesViewModelTest {

    private class FakeGetUserCourses : GetUserCourses {
        var invokeCount = 0
        var result: DomainResult<List<UserCourse>> = DomainResult.Success(emptyList())

        override suspend fun invoke(): DomainResult<List<UserCourse>> {
            invokeCount++
            return result
        }
    }

    private lateinit var fakeGetUserCourses: FakeGetUserCourses
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: UserCoursesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGetUserCourses = FakeGetUserCourses()
        viewModel = UserCoursesViewModel(
            getUserCourses = fakeGetUserCourses,
            dispatcher = testDispatcher,
        )
    }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading then success with courses`() = runTest(testDispatcher) {
        val id1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val id2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
        fakeGetUserCourses.result = DomainResult.Success(
            listOf(
                UserCourse(CourseId(id1), "Course 1", CourseRole.TEACHER),
                UserCourse(CourseId(id2), "Course 2", CourseRole.STUDENT),
            ),
        )

        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.courses.size)
        assertEquals("Course 1", state.courses[0].title)
        assertEquals(CourseRole.TEACHER, state.courses[0].role)
        assertEquals("Course 2", state.courses[1].title)
        assertEquals(CourseRole.STUDENT, state.courses[1].role)
        assertEquals(1, fakeGetUserCourses.invokeCount)
    }

    @Test
    fun `initial load failure sets error and clears loading`() = runTest(testDispatcher) {
        fakeGetUserCourses.result = DomainResult.Failure(DomainError.Unknown())

        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.courses.isEmpty())
    }

    @Test
    fun `onRetry calls use case again and updates state`() = runTest(testDispatcher) {
        fakeGetUserCourses.result = DomainResult.Failure(DomainError.Unknown())

        viewModel.onRetry()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        fakeGetUserCourses.result = DomainResult.Success(
            listOf(UserCourse(CourseId(UUID.randomUUID()), "Single", CourseRole.STUDENT)),
        )
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(2, fakeGetUserCourses.invokeCount)
        val state = viewModel.uiState.value
        assertEquals(1, state.courses.size)
        assertEquals("Single", state.courses[0].title)
        assertNull(state.error)
    }
}
