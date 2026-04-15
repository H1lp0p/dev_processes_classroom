package com.stuf.classroom.auth

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD-спека для RegisterViewModel.
 *
 * Задаёт контракт:
 * - RegisterUiState
 * - onCredentialsChanged / onEmailChanged / onPasswordChanged / onRepeatPasswordChanged
 * - onRegisterClick с базовой валидацией и вызовом AuthRepository.register.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private class FakeAuthRepository : AuthRepository {
        var lastCredentials: String? = null
        var lastEmail: String? = null
        var lastPassword: String? = null
        var registerResult: DomainResult<com.stuf.domain.repository.AuthSession> =
            DomainResult.Failure(DomainError.Unknown())

        override suspend fun login(
            email: String,
            password: String,
        ): DomainResult<com.stuf.domain.repository.AuthSession> {
            error("Not used in RegisterViewModel tests")
        }

        override suspend fun register(
            credentials: String,
            email: String,
            password: String,
        ): DomainResult<com.stuf.domain.repository.AuthSession> {
            lastCredentials = credentials
            lastEmail = email
            lastPassword = password
            return registerResult
        }

        override suspend fun refresh(): DomainResult<com.stuf.domain.repository.AuthSession> {
            error("Not used in RegisterViewModel tests")
        }

        override suspend fun changePassword(oldPassword: String, newPassword: String): DomainResult<Unit> {
            error("Not used in RegisterViewModel tests")
        }
    }

    private class FakeAuthManager : AuthManager {
        var successCount: Int = 0

        override val authState: kotlinx.coroutines.flow.StateFlow<AuthState>
            get() = error("Not needed in RegisterViewModel tests")

        override suspend fun initialize() {
            error("Not needed in RegisterViewModel tests")
        }

        override suspend fun logout() {
            error("Not needed in RegisterViewModel tests")
        }

        override fun onAuthSuccess(session: AuthSession) {
            successCount++
        }
    }

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthManager: FakeAuthManager
    private val testDispatcher = StandardTestDispatcher()

    // Реализация RegisterViewModel должна иметь такой конструктор:
    // RegisterViewModel(
    //     authRepository: AuthRepository,
    //     authManager: AuthManager,
    //     dispatcher: CoroutineDispatcher = Dispatchers.Main,
    // )
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthManager = FakeAuthManager()
        viewModel = RegisterViewModel(
            authRepository = fakeAuthRepository,
            authManager = fakeAuthManager,
            dispatcher = testDispatcher,
        )
    }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`(): Unit = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertEquals("", state.credentials)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.repeatPassword)
        assertFalse(state.isLoading)
        assertNullOrEmpty(state.credentialsError)
        assertNullOrEmpty(state.emailError)
        assertNullOrEmpty(state.passwordError)
        assertNullOrEmpty(state.repeatPasswordError)
        assertNullOrEmpty(state.generalError)
        assertFalse(state.isRegistered)
    }

    @Test
    fun `input handlers update fields`(): Unit = runTest(testDispatcher) {
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onRepeatPasswordChanged("123456")

        val state = viewModel.uiState.value
        assertEquals("User Name", state.credentials)
        assertEquals("user@example.com", state.email)
        assertEquals("123456", state.password)
        assertEquals("123456", state.repeatPassword)
    }

    @Test
    fun `empty fields show validation errors and do not call repository`(): Unit = runTest(testDispatcher) {
        viewModel.onCredentialsChanged("")
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onRepeatPasswordChanged("")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.credentialsError.isNullOrEmpty())
        assertTrue(!state.emailError.isNullOrEmpty())
        assertTrue(!state.passwordError.isNullOrEmpty())
        assertTrue(!state.repeatPasswordError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastEmail)
    }

    @Test
    fun `invalid email does not call repository and sets email error`(): Unit = runTest(testDispatcher) {
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("123456")
        viewModel.onRepeatPasswordChanged("123456")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.emailError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastEmail)
    }

    @Test
    fun `short password does not call repository and sets password error`(): Unit = runTest(testDispatcher) {
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123")
        viewModel.onRepeatPasswordChanged("123")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.passwordError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastEmail)
    }

    @Test
    fun `mismatched passwords do not call repository and set repeatPasswordError`(): Unit = runTest(testDispatcher) {
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onRepeatPasswordChanged("654321")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.repeatPasswordError.isNullOrEmpty())
        assertEquals(null, fakeAuthRepository.lastEmail)
    }

    @Test
    fun `successful registration calls repository, clears errors, sets isRegistered and toggles loading`(): Unit = runTest(testDispatcher) {
        fakeAuthRepository.registerResult = DomainResult.Success(
            AuthSession(
                accessToken = "access",
                refreshToken = "refresh",
            ),
        )
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onRepeatPasswordChanged("123456")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("User Name", fakeAuthRepository.lastCredentials)
        assertEquals("user@example.com", fakeAuthRepository.lastEmail)
        assertEquals("123456", fakeAuthRepository.lastPassword)
        assertFalse(state.isLoading)
        assertNullOrEmpty(state.credentialsError)
        assertNullOrEmpty(state.emailError)
        assertNullOrEmpty(state.passwordError)
        assertNullOrEmpty(state.repeatPasswordError)
        assertNullOrEmpty(state.generalError)
        assertTrue(state.isRegistered)
        // успешная регистрация должна оповестить глобальный auth-слой
        assertEquals(1, fakeAuthManager.successCount)
    }

    @Test
    fun `failed registration shows general error and does not mark user as registered`(): Unit = runTest(testDispatcher) {
        fakeAuthRepository.registerResult = DomainResult.Failure(DomainError.Validation("Email already in use"))
        viewModel.onCredentialsChanged("User Name")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onRepeatPasswordChanged("123456")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@example.com", fakeAuthRepository.lastEmail)
        assertFalse(state.isLoading)
        assertTrue(!state.generalError.isNullOrEmpty())
        assertFalse(state.isRegistered)
        // при ошибке регистрации глобальный auth-слой не должен получать успешное событие
        assertEquals(0, fakeAuthManager.successCount)
    }

    private fun assertNullOrEmpty(value: String?) {
        assertTrue(value.isNullOrEmpty())
    }
}

