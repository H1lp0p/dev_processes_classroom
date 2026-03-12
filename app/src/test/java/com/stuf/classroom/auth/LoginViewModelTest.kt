package com.stuf.classroom.auth

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD-спека для LoginViewModel.
 *
 * Задаёт контракт:
 * - LoginUiState как источник состояния
 * - обработчики onEmailChanged / onPasswordChanged / onLoginClick
 * - базовая валидация + работа с AuthRepository через корутины.
 *
 * Реализация LoginViewModel должна удовлетворять этим тестам.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuthRepository : AuthRepository {
        var lastLoginEmail: String? = null
        var lastLoginPassword: String? = null
        var loginResult: DomainResult<com.stuf.domain.repository.AuthSession> =
            DomainResult.Failure(DomainError.Unknown)

        override suspend fun login(
            email: String,
            password: String,
        ): DomainResult<com.stuf.domain.repository.AuthSession> {
            lastLoginEmail = email
            lastLoginPassword = password
            return loginResult
        }

        override suspend fun register(
            credentials: String,
            email: String,
            password: String,
        ): DomainResult<com.stuf.domain.repository.AuthSession> {
            error("Not used in LoginViewModel tests")
        }

        override suspend fun refresh(): DomainResult<com.stuf.domain.repository.AuthSession> {
            error("Not used in LoginViewModel tests")
        }
    }

    private class FakeAuthManager : AuthManager {
        var successCount: Int = 0

        override val authState: kotlinx.coroutines.flow.StateFlow<AuthState>
            get() = error("Not needed in LoginViewModel tests")

        override suspend fun initialize() {
            error("Not needed in LoginViewModel tests")
        }

        override suspend fun logout() {
            error("Not needed in LoginViewModel tests")
        }

        override fun onAuthSuccess(session: AuthSession) {
            successCount++
        }
    }

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthManager: FakeAuthManager
    private val testDispatcher = StandardTestDispatcher()

    // Реализация LoginViewModel должна иметь такой конструктор:
    // LoginViewModel(
    //     authRepository: AuthRepository,
    //     authManager: AuthManager,
    //     dispatcher: CoroutineDispatcher = Dispatchers.Main,
    // )
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthManager = FakeAuthManager()
        viewModel = LoginViewModel(
            authRepository = fakeAuthRepository,
            authManager = fakeAuthManager,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `initial state is empty and not loading`(): Unit = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNullOrEmpty(state.emailError)
        assertNullOrEmpty(state.passwordError)
        assertNullOrEmpty(state.generalError)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `email and password are updated on input`(): Unit = runTest(testDispatcher) {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")

        val state = viewModel.uiState.value
        assertEquals("user@example.com", state.email)
        assertEquals("123456", state.password)
    }

    @Test
    fun `login with empty fields shows validation errors and does not call repository`(): Unit = runTest(testDispatcher) {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")

        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.emailError.isNullOrEmpty())
        assertTrue(!state.passwordError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastLoginEmail)
        assertEquals(null, fakeAuthRepository.lastLoginPassword)
    }

    @Test
    fun `login with invalid email does not call repository and sets email error`(): Unit = runTest(testDispatcher) {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("123456")

        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.emailError.isNullOrEmpty())
        assertNullOrEmpty(state.passwordError)
        assertEquals(null, fakeAuthRepository.lastLoginEmail)
    }

    @Test
    fun `login with short password does not call repository and sets password error`(): Unit = runTest(testDispatcher) {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123")

        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNullOrEmpty(state.emailError)
        assertTrue(!state.passwordError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastLoginEmail)
    }

    @Test
    fun `successful login calls repository, clears errors, sets isLoggedIn and toggles loading`(): Unit = runTest(testDispatcher) {
        fakeAuthRepository.loginResult = DomainResult.Success(
            AuthSession(
                accessToken = "access",
                refreshToken = "refresh",
            ),
        )
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")

        viewModel.onLoginClick()

        // Поскольку используется testDispatcher, вручную дожидаемся завершения корутин
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@example.com", fakeAuthRepository.lastLoginEmail)
        assertEquals("123456", fakeAuthRepository.lastLoginPassword)
        assertFalse(state.isLoading)
        assertNullOrEmpty(state.emailError)
        assertNullOrEmpty(state.passwordError)
        assertNullOrEmpty(state.generalError)
        assertTrue(state.isLoggedIn)
        // успешный логин должен оповестить глобальный auth-слой
        assertEquals(1, fakeAuthManager.successCount)
    }

    @Test
    fun `failed login shows general error and does not mark user as logged in`(): Unit = runTest(testDispatcher) {
        fakeAuthRepository.loginResult = DomainResult.Failure(DomainError.InvalidCredentials)
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")

        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@example.com", fakeAuthRepository.lastLoginEmail)
        assertFalse(state.isLoading)
        assertTrue(!state.generalError.isNullOrEmpty())
        assertFalse(state.isLoggedIn)
        // глобальный auth-слой не должен считаться успешным при ошибке
        assertEquals(0, fakeAuthManager.successCount)
    }

    private fun assertNullOrEmpty(value: String?) {
        assertTrue(value.isNullOrEmpty())
    }
}

