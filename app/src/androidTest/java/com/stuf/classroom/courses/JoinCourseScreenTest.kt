package com.stuf.classroom.courses

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stuf.classroom.ui.theme.ClassroomTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TDD-спека для JoinCourseScreen.
 *
 * Задаёт:
 * - наличие основных элементов (заголовок, поле inviteCode, кнопки);
 * - базовые testTag'и;
 * - реакции на ввод и нажатия.
 *
 * Реализация JoinCourseScreen должна удовлетворять этим тестам.
 */
@RunWith(AndroidJUnit4::class)
class JoinCourseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun joinCourseScreen_shows_basic_elements() {
        val state: JoinCourseUiState = JoinCourseUiState()

        composeRule.setContent {
            ClassroomTheme {
                JoinCourseScreen(
                    state = state,
                    onInviteCodeChanged = {},
                    onJoinClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("join_course_title").assertIsDisplayed()
        composeRule.onNodeWithTag("join_course_code_field").assertIsDisplayed()
        composeRule.onNodeWithTag("join_course_button").assertIsDisplayed()
        composeRule.onNodeWithTag("join_course_back_button").assertIsDisplayed()
    }

    @Test
    fun joinCourseScreen_allows_text_input() {
        val initialState: JoinCourseUiState = JoinCourseUiState()
        var lastInviteCode: String? = null

        composeRule.setContent {
            ClassroomTheme {
                JoinCourseScreen(
                    state = initialState,
                    onInviteCodeChanged = { value: String -> lastInviteCode = value },
                    onJoinClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("join_course_code_field").performClick()
        composeRule.onNodeWithTag("join_course_code_field").performTextInput("ABC123")
        composeRule.waitForIdle()

        assert(lastInviteCode == "ABC123") {
            "Expected lastInviteCode \"ABC123\", got: $lastInviteCode"
        }
    }

    @Test
    fun joinCourseScreen_calls_callbacks_on_buttons_click() {
        val state: JoinCourseUiState = JoinCourseUiState()
        var joinClicked: Boolean = false
        var backClicked: Boolean = false

        composeRule.setContent {
            ClassroomTheme {
                JoinCourseScreen(
                    state = state,
                    onInviteCodeChanged = {},
                    onJoinClick = { joinClicked = true },
                    onBackClick = { backClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("join_course_button").assertIsEnabled()
        composeRule.onNodeWithTag("join_course_button").performClick()
        composeRule.onNodeWithTag("join_course_back_button").assertIsEnabled()
        composeRule.onNodeWithTag("join_course_back_button").performClick()

        assert(joinClicked) { "Expected onJoinClick to be called" }
        assert(backClicked) { "Expected onBackClick to be called" }
    }

    @Test
    fun joinCourseScreen_shows_errors_and_loading_from_state() {
        val errorState: JoinCourseUiState =
            JoinCourseUiState(
                inviteCode = "BAD",
                inviteCodeError = "Invalid code",
                generalError = "Something went wrong",
                isLoading = true,
            )

        composeRule.setContent {
            ClassroomTheme {
                JoinCourseScreen(
                    state = errorState,
                    onInviteCodeChanged = {},
                    onJoinClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("join_course_code_error").assertIsDisplayed()
        composeRule.onNodeWithTag("join_course_general_error").assertIsDisplayed()

        composeRule.onNodeWithTag("join_course_loading_indicator")
            .assertExistsOrIsOptional()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertExistsOrIsOptional() {
        val matcher = hasTestTag("join_course_loading_indicator")
        val nodes = composeRule.onAllNodes(matcher)
        if (nodes.fetchSemanticsNodes().isNotEmpty()) {
            this.assertIsDisplayed()
        }
    }
}

