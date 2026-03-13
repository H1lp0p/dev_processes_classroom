package com.stuf.classroom.courses

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stuf.classroom.ui.theme.ClassroomTheme
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * TDD-спека для UserCoursesScreen.
 *
 * Задаёт:
 * - список курсов (название + badge роли);
 * - FAB с опциями «Новый курс» и «Присоединиться»;
 * - отображение загрузки, ошибки и кнопки «Повторить»;
 * - вызов колбэков onRetry, onNewCourse, onJoinCourse.
 */
@RunWith(AndroidJUnit4::class)
class UserCoursesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun userCoursesScreen_shows_basic_elements() {
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(courses = emptyList()),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_list").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_fab").assertIsDisplayed()
    }

    @Test
    fun userCoursesScreen_shows_courses_with_titles_and_roles() {
        val courses = listOf(
            UserCourse(CourseId(UUID.fromString("00000000-0000-0000-0000-000000000001")), "Курс 1", CourseRole.TEACHER),
            UserCourse(CourseId(UUID.fromString("00000000-0000-0000-0000-000000000002")), "Курс 2", CourseRole.STUDENT),
        )
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(courses = courses),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_item_0").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_item_1").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_list").assertIsDisplayed()
    }

    @Test
    fun userCoursesScreen_shows_loading_and_error_from_state() {
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(
                        isLoading = true,
                        error = "Ошибка загрузки",
                    ),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_error").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_retry").assertIsDisplayed()
    }

    @Test
    fun userCoursesScreen_fab_reveals_new_course_and_join() {
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_fab").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("user_courses_new_course").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_join").assertIsDisplayed()
    }

    @Test
    fun userCoursesScreen_calls_onNewCourse_when_new_course_clicked() {
        var newCourseClicked = false
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(),
                    onRetry = {},
                    onNewCourse = { newCourseClicked = true },
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_fab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("user_courses_new_course").assertIsEnabled()
        composeRule.onNodeWithTag("user_courses_new_course").performClick()

        assert(newCourseClicked) { "Expected onNewCourse to be called" }
    }

    @Test
    fun userCoursesScreen_calls_onJoinCourse_when_join_clicked() {
        var joinClicked = false
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = { joinClicked = true },
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_fab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("user_courses_join").assertIsEnabled()
        composeRule.onNodeWithTag("user_courses_join").performClick()

        assert(joinClicked) { "Expected onJoinCourse to be called" }
    }

    @Test
    fun userCoursesScreen_calls_onRetry_when_retry_clicked() {
        var retryClicked = false
        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(error = "Ошибка"),
                    onRetry = { retryClicked = true },
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_retry").assertIsEnabled()
        composeRule.onNodeWithTag("user_courses_retry").performClick()

        assert(retryClicked) { "Expected onRetry to be called" }
    }

    @Test
    fun userCoursesScreen_calls_onCourseClick_when_item_clicked() {
        val courseId = UUID.fromString("00000000-0000-0000-0000-000000000010")
        val courses = listOf(
            UserCourse(CourseId(courseId), "Курс 1", CourseRole.TEACHER),
        )
        var clickedCourse: UserCourse? = null

        composeRule.setContent {
            ClassroomTheme {
                UserCoursesScreen(
                    state = UserCoursesUiState(courses = courses),
                    onRetry = {},
                    onNewCourse = {},
                    onJoinCourse = {},
                    onLogout = null,
                    onCourseClick = { clickedCourse = it },
                )
            }
        }

        composeRule.onNodeWithTag("user_courses_item_0").assertIsDisplayed()
        composeRule.onNodeWithTag("user_courses_item_0").performClick()

        assert(clickedCourse != null) { "Expected onCourseClick to be called" }
        assert(clickedCourse?.id?.value == courseId) { "Expected clicked course id $courseId, got ${clickedCourse?.id?.value}" }
    }
}
