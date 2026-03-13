package com.stuf.classroom.courses

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.usecase.JoinCourse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * TDD-спека для JoinCourseViewModel.
 *
 * Задаёт контракт:
 * - JoinCourseUiState как источник состояния;
 * - обработчики onInviteCodeChanged / onJoinClick;
 * - базовая валидация инвайт-кода + работа с use-case JoinCourse через корутины.
 *
 * Реализация JoinCourseViewModel должна удовлетворять этим тестам.
 */
class JoinCourseViewModelTest {

    private class FakeJoinCourse : JoinCourse {
        var lastInviteCode: String? = null
        var result: DomainResult<Course> =
            DomainResult.Success(
                Course(
                    id = CourseId(UUID.fromString("00000000-0000-0000-0000-0000000000BA")),
                    title = "Joined course",
                    inviteCode = "CODE",
                    authorId = null,
                ),
            )
        var invokeCount: Int = 0

        override suspend fun invoke(inviteCode: String): DomainResult<Course> {
            invokeCount++
            lastInviteCode = inviteCode
            return result
        }
    }

    private lateinit var fakeJoinCourse: FakeJoinCourse

    // Реализация JoinCourseViewModel должна иметь такой конструктор:
    // JoinCourseViewModel(
    //     joinCourse: JoinCourse,
    //     dispatcher: CoroutineDispatcher = Dispatchers.Main,
    // )
    private lateinit var joinCourseViewModel: JoinCourseViewModel

    @Before
    fun setUp() {
        fakeJoinCourse = FakeJoinCourse()
        joinCourseViewModel = JoinCourseViewModel(
            joinCourse = fakeJoinCourse,
            dispatcher = Dispatchers.Unconfined,
        )
    }

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val state: JoinCourseUiState = joinCourseViewModel.uiState.value

        assertEquals("", state.inviteCode)
        assertFalse(state.isLoading)
        assertTrue(state.inviteCodeError.isNullOrEmpty())
        assertTrue(state.generalError.isNullOrEmpty())
        assertFalse(state.isJoined)
    }

    @Test
    fun `invite code is updated on input`() = runTest {
        joinCourseViewModel.onInviteCodeChanged("ABC123")

        val state: JoinCourseUiState = joinCourseViewModel.uiState.value
        assertEquals("ABC123", state.inviteCode)
    }

    @Test
    fun `join with empty invite code shows validation error and does not call use case`() = runTest {
        joinCourseViewModel.onInviteCodeChanged("")

        joinCourseViewModel.onJoinClick()
        advanceUntilIdle()

        val state: JoinCourseUiState = joinCourseViewModel.uiState.value
        assertTrue(!state.inviteCodeError.isNullOrEmpty())
        assertEquals(0, fakeJoinCourse.invokeCount)
        assertFalse(state.isJoined)
    }

    @Test
    fun `successful join calls use case, clears errors, sets isJoined and toggles loading`() = runTest {
        val expectedCode: String = "INVITE123"
        fakeJoinCourse.result =
            DomainResult.Success(
                Course(
                    id = CourseId(UUID.fromString("00000000-0000-0000-0000-0000000000BB")),
                    title = "Joined",
                    inviteCode = expectedCode,
                    authorId = null,
                ),
            )

        joinCourseViewModel.onInviteCodeChanged("  $expectedCode  ")
        joinCourseViewModel.onJoinClick()

        advanceUntilIdle()

        val state: JoinCourseUiState = joinCourseViewModel.uiState.value
        assertEquals(1, fakeJoinCourse.invokeCount)
        assertEquals(expectedCode, fakeJoinCourse.lastInviteCode)
        assertFalse(state.isLoading)
        assertTrue(state.inviteCodeError.isNullOrEmpty())
        assertTrue(state.generalError.isNullOrEmpty())
        assertTrue(state.isJoined)
    }

    @Test
    fun `failed join shows general error and does not mark as joined`() = runTest {
        fakeJoinCourse.result = DomainResult.Failure(DomainError.Unknown())

        joinCourseViewModel.onInviteCodeChanged("CODE")
        joinCourseViewModel.onJoinClick()

        advanceUntilIdle()

        val state: JoinCourseUiState = joinCourseViewModel.uiState.value
        assertEquals(1, fakeJoinCourse.invokeCount)
        assertFalse(state.isLoading)
        assertTrue(!state.generalError.isNullOrEmpty())
        assertFalse(state.isJoined)
    }
}

