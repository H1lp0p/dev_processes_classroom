package com.stuf.classroom.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
 * TDD-спека для RegisterScreen.
 *
 * Задаёт:
 * - основные элементы формы регистрации;
 * - testTag'и;
 * - реакции на ввод и нажатия.
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registerScreen_shows_basic_elements() {
        composeRule.setContent {
            ClassroomTheme {
                RegisterScreen(
                    state = RegisterUiState(),
                    onCredentialsChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onRepeatPasswordChanged = {},
                    onRegisterClick = {},
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.onNodeWithTag("register_title").assertIsDisplayed()
        composeRule.onNodeWithTag("register_credentials_field").assertIsDisplayed()
        composeRule.onNodeWithTag("register_email_field").assertIsDisplayed()
        composeRule.onNodeWithTag("register_password_field").assertIsDisplayed()
        composeRule.onNodeWithTag("register_repeat_password_field").assertIsDisplayed()
        composeRule.onNodeWithTag("register_button").assertIsDisplayed()
        composeRule.onNodeWithTag("register_to_login_button").assertIsDisplayed()
    }

    @Test
    fun registerScreen_allows_text_input() {
        var lastCredentials: String? = null
        var lastEmail: String? = null
        var lastPassword: String? = null
        var lastRepeatPassword: String? = null

        composeRule.setContent {
            ClassroomTheme {
                RegisterScreen(
                    state = RegisterUiState(),
                    onCredentialsChanged = { lastCredentials = it },
                    onEmailChanged = { lastEmail = it },
                    onPasswordChanged = { lastPassword = it },
                    onRepeatPasswordChanged = { lastRepeatPassword = it },
                    onRegisterClick = {},
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.onNodeWithTag("register_credentials_field").performTextInput("User Name")
        composeRule.onNodeWithTag("register_email_field").performTextInput("user@example.com")
        composeRule.onNodeWithTag("register_password_field").performTextInput("123456")
        composeRule.onNodeWithTag("register_repeat_password_field").performTextInput("123456")

        assert(lastCredentials == "User Name")
        assert(lastEmail == "user@example.com")
        assert(lastPassword == "123456")
        assert(lastRepeatPassword == "123456")
    }

    @Test
    fun registerScreen_calls_register_and_navigation_callbacks() {
        var registerClicked = false
        var navigateToLoginClicked = false

        composeRule.setContent {
            ClassroomTheme {
                RegisterScreen(
                    state = RegisterUiState(),
                    onCredentialsChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onRepeatPasswordChanged = {},
                    onRegisterClick = { registerClicked = true },
                    onNavigateToLogin = { navigateToLoginClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("register_button").assertIsEnabled()
        composeRule.onNodeWithTag("register_button").performClick()
        composeRule.onNodeWithTag("register_to_login_button").performClick()

        assert(registerClicked)
        assert(navigateToLoginClicked)
    }

    @Test
    fun registerScreen_shows_errors_and_loading_from_state() {
        val errorState = RegisterUiState(
            credentials = "User Name",
            email = "bad",
            password = "123",
            repeatPassword = "321",
            credentialsError = "Required",
            emailError = "Invalid email",
            passwordError = "Too short",
            repeatPasswordError = "Passwords do not match",
            generalError = "Something went wrong",
            isLoading = true,
        )

        composeRule.setContent {
            ClassroomTheme {
                RegisterScreen(
                    state = errorState,
                    onCredentialsChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onRepeatPasswordChanged = {},
                    onRegisterClick = {},
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.onNodeWithTag("register_credentials_error").assertIsDisplayed()
        composeRule.onNodeWithTag("register_email_error").assertIsDisplayed()
        composeRule.onNodeWithTag("register_password_error").assertIsDisplayed()
        composeRule.onNodeWithTag("register_repeat_password_error").assertIsDisplayed()
        composeRule.onNodeWithTag("register_general_error").assertIsDisplayed()

        // Аналогично login-экрану — индикатор загрузки минимально обязателен только как testTag.
        composeRule.onNodeWithTag("register_loading_indicator").assertIsDisplayed()
    }
}

