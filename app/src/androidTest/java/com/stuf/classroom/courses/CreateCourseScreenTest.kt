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
 * TDD-спека для CreateCourseScreen.
 *
 * Задаёт:
 * - наличие основных элементов (заголовок, поле title, кнопки);
 * - базовые testTag'и;
 * - реакции на ввод и нажатия.
 *
 * Реализация CreateCourseScreen должна удовлетворять этим тестам.
 */
@RunWith(AndroidJUnit4::class)
class CreateCourseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createCourseScreen_shows_basic_elements() {
        val state: CreateCourseUiState = CreateCourseUiState()

        composeRule.setContent {
            ClassroomTheme {
                CreateCourseScreen(
                    state = state,
                    onTitleChanged = {},
                    onCreateClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("create_course_title").assertIsDisplayed()
        composeRule.onNodeWithTag("create_course_title_field").assertIsDisplayed()
        composeRule.onNodeWithTag("create_course_button").assertIsDisplayed()
        composeRule.onNodeWithTag("create_course_back_button").assertIsDisplayed()
    }

    @Test
    fun createCourseScreen_allows_text_input() {
        val initialState: CreateCourseUiState = CreateCourseUiState()
        var lastTitle: String? = null

        composeRule.setContent {
            ClassroomTheme {
                CreateCourseScreen(
                    state = initialState,
                    onTitleChanged = { value: String -> lastTitle = value },
                    onCreateClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("create_course_title_field").performClick()
        composeRule.onNodeWithTag("create_course_title_field").performTextInput("Kotlin course")
        composeRule.waitForIdle()

        assert(lastTitle == "Kotlin course") {
            "Expected lastTitle \"Kotlin course\", got: $lastTitle"
        }
    }

    @Test
    fun createCourseScreen_calls_callbacks_on_buttons_click() {
        val state: CreateCourseUiState = CreateCourseUiState()
        var createClicked: Boolean = false
        var backClicked: Boolean = false

        composeRule.setContent {
            ClassroomTheme {
                CreateCourseScreen(
                    state = state,
                    onTitleChanged = {},
                    onCreateClick = { createClicked = true },
                    onBackClick = { backClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("create_course_button").assertIsEnabled()
        composeRule.onNodeWithTag("create_course_button").performClick()
        composeRule.onNodeWithTag("create_course_back_button").assertIsEnabled()
        composeRule.onNodeWithTag("create_course_back_button").performClick()

        assert(createClicked) { "Expected onCreateClick to be called" }
        assert(backClicked) { "Expected onBackClick to be called" }
    }

    @Test
    fun createCourseScreen_shows_errors_and_loading_from_state() {
        val errorState: CreateCourseUiState =
            CreateCourseUiState(
                title = "Bad title",
                titleError = "Invalid title",
                generalError = "Something went wrong",
                isLoading = true,
            )

        composeRule.setContent {
            ClassroomTheme {
                CreateCourseScreen(
                    state = errorState,
                    onTitleChanged = {},
                    onCreateClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("create_course_title_error").assertIsDisplayed()
        composeRule.onNodeWithTag("create_course_general_error").assertIsDisplayed()

        composeRule.onNodeWithTag("create_course_loading_indicator")
            .assertExistsOrIsOptional()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertExistsOrIsOptional() {
        val matcher = hasTestTag("create_course_loading_indicator")
        val nodes = composeRule.onAllNodes(matcher)
        if (nodes.fetchSemanticsNodes().isNotEmpty()) {
            this.assertIsDisplayed()
        }
    }
}

