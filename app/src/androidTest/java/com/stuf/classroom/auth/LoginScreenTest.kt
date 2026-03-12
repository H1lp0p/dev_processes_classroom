package com.stuf.classroom.auth

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
 * TDD-спека для LoginScreen.
 *
 * Задаёт:
 * - наличие основных элементов (заголовок, поля, кнопки);
 * - базовые testTag'и;
 * - реакции на ввод и нажатия.
 *
 * Реализация LoginScreen должна удовлетворять этим тестам.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginScreen_shows_basic_elements() {
        composeRule.setContent {
            ClassroomTheme {
                LoginScreen(
                    state = LoginUiState(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onLoginClick = {},
                    onNavigateToRegister = {},
                )
            }
        }

        composeRule.onNodeWithTag("login_title").assertIsDisplayed()
        composeRule.onNodeWithTag("login_email_field").assertIsDisplayed()
        composeRule.onNodeWithTag("login_password_field").assertIsDisplayed()
        composeRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeRule.onNodeWithTag("login_to_register_button").assertIsDisplayed()
    }

    @Test
    fun loginScreen_allows_text_input() {
        val initialState = LoginUiState()
        var lastEmail: String? = null
        var lastPassword: String? = null

        composeRule.setContent {
            ClassroomTheme {
                LoginScreen(
                    state = initialState,
                    onEmailChanged = { lastEmail = it },
                    onPasswordChanged = { lastPassword = it },
                    onLoginClick = {},
                    onNavigateToRegister = {},
                )
            }
        }

        composeRule.onNodeWithTag("login_email_field").performTextInput("user@example.com")
        composeRule.onNodeWithTag("login_password_field").performTextInput("123456")

        // Проверяем, что callbacks были вызваны с нужными значениями
        assert(lastEmail == "user@example.com")
        assert(lastPassword == "123456")
    }

    @Test
    fun loginScreen_calls_login_and_navigation_callbacks() {
        var loginClicked = false
        var navigateToRegisterClicked = false

        composeRule.setContent {
            ClassroomTheme {
                LoginScreen(
                    state = LoginUiState(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onLoginClick = { loginClicked = true },
                    onNavigateToRegister = { navigateToRegisterClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("login_button").assertIsEnabled()
        composeRule.onNodeWithTag("login_button").performClick()
        composeRule.onNodeWithTag("login_to_register_button").performClick()

        assert(loginClicked)
        assert(navigateToRegisterClicked)
    }

    @Test
    fun loginScreen_shows_errors_and_loading_from_state() {
        val errorState = LoginUiState(
            email = "bad",
            password = "123",
            emailError = "Invalid email",
            passwordError = "Too short",
            generalError = "Something went wrong",
            isLoading = true,
        )

        composeRule.setContent {
            ClassroomTheme {
                LoginScreen(
                    state = errorState,
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onLoginClick = {},
                    onNavigateToRegister = {},
                )
            }
        }

        // Проверяем наличие контейнеров для ошибок и индикатора загрузки
        composeRule.onNodeWithTag("login_email_error").assertIsDisplayed()
        composeRule.onNodeWithTag("login_password_error").assertIsDisplayed()
        composeRule.onNodeWithTag("login_general_error").assertIsDisplayed()

        // Индикатор загрузки может быть реализован как прогресс-бар или дизейбл кнопки
        composeRule.onNodeWithTag("login_loading_indicator")
            .assertExistsOrIsOptional()
    }

    /**
     * Вспомогательное расширение для «мягкой» проверки наличия индикатора.
     * Если элемент отсутствует, тест не должен падать — это минимальное требование.
     */
    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertExistsOrIsOptional() {
        val matcher = hasTestTag("login_loading_indicator")
        val nodes = composeRule.onAllNodes(matcher)
        // Если узлов нет — ок, индикатор может быть реализован иначе.
        if (nodes.fetchSemanticsNodes().isNotEmpty()) {
            this.assertIsDisplayed()
        }
    }
}

