package com.stuf.classroom.courses

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.usecase.CreateCourse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * TDD-спека для CreateCourseViewModel.
 *
 * Задаёт контракт:
 * - CreateCourseUiState как источник состояния;
 * - обработчики onTitleChanged / onCreateClick;
 * - базовая валидация + работа с use-case CreateCourse через корутины.
 *
 * Реализация CreateCourseViewModel должна удовлетворять этим тестам.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateCourseViewModelTest {

    private class FakeCreateCourse : CreateCourse {
        var lastTitle: String? = null
        var result: DomainResult<Course> =
            DomainResult.Success(
                Course(
                    id = CourseId(UUID.fromString("00000000-0000-0000-0000-0000000000AA")),
                    title = "Default title",
                    inviteCode = null,
                    authorId = null,
                ),
            )
        var invokeCount: Int = 0

        override suspend fun invoke(title: String): DomainResult<Course> {
            invokeCount++
            lastTitle = title
            return result
        }
    }

    private lateinit var fakeCreateCourse: FakeCreateCourse
    private val testDispatcher: StandardTestDispatcher = StandardTestDispatcher()

    // Реализация CreateCourseViewModel должна иметь такой конструктор:
    // CreateCourseViewModel(
    //     createCourse: CreateCourse,
    //     dispatcher: CoroutineDispatcher = Dispatchers.Main,
    // )
    private lateinit var createCourseViewModel: CreateCourseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCreateCourse = FakeCreateCourse()
        createCourseViewModel = CreateCourseViewModel(
            createCourse = fakeCreateCourse,
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() = runTest(testDispatcher) {
        val state: CreateCourseUiState = createCourseViewModel.uiState.value

        assertEquals("", state.title)
        assertFalse(state.isLoading)
        assertTrue(state.titleError.isNullOrEmpty())
        assertTrue(state.generalError.isNullOrEmpty())
        assertFalse(state.isCreated)
    }

    @Test
    fun `title is updated on input`() = runTest(testDispatcher) {
        createCourseViewModel.onTitleChanged("Kotlin course")

        val state: CreateCourseUiState = createCourseViewModel.uiState.value
        assertEquals("Kotlin course", state.title)
    }

    @Test
    fun `create with empty title shows validation error and does not call use case`() = runTest(testDispatcher) {
        createCourseViewModel.onTitleChanged("")

        createCourseViewModel.onCreateClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state: CreateCourseUiState = createCourseViewModel.uiState.value
        assertTrue(!state.titleError.isNullOrEmpty())
        assertEquals(0, fakeCreateCourse.invokeCount)
        assertFalse(state.isCreated)
    }

    @Test
    fun `successful create calls use case, clears errors, sets isCreated and toggles loading`() = runTest(testDispatcher) {
        val expectedTitle: String = "Kotlin course"
        fakeCreateCourse.result =
            DomainResult.Success(
                Course(
                    id = CourseId(UUID.fromString("00000000-0000-0000-0000-0000000000AB")),
                    title = expectedTitle,
                    inviteCode = null,
                    authorId = null,
                ),
            )

        createCourseViewModel.onTitleChanged("  $expectedTitle  ")
        createCourseViewModel.onCreateClick()

        testDispatcher.scheduler.advanceUntilIdle()

        val state: CreateCourseUiState = createCourseViewModel.uiState.value
        assertEquals(1, fakeCreateCourse.invokeCount)
        assertEquals(expectedTitle, fakeCreateCourse.lastTitle)
        assertFalse(state.isLoading)
        assertTrue(state.titleError.isNullOrEmpty())
        assertTrue(state.generalError.isNullOrEmpty())
        assertTrue(state.isCreated)
    }

    @Test
    fun `failed create shows general error and does not mark as created`() = runTest(testDispatcher) {
        fakeCreateCourse.result = DomainResult.Failure(DomainError.Unknown())

        createCourseViewModel.onTitleChanged("Some title")
        createCourseViewModel.onCreateClick()

        testDispatcher.scheduler.advanceUntilIdle()

        val state: CreateCourseUiState = createCourseViewModel.uiState.value
        assertEquals(1, fakeCreateCourse.invokeCount)
        assertFalse(state.isLoading)
        assertTrue(!state.generalError.isNullOrEmpty())
        assertFalse(state.isCreated)
    }
}

