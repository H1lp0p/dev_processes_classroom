package com.stuf.classroom.auth

import com.stuf.data.auth.AuthSessionStorage
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD-спека для глобального слоя авторизации приложения.
 *
 * Задаёт контракт:
 * - sealed-класс AuthState (Unauthenticated, Loading, Authenticated(AuthSession))
 * - класс AuthManager с конструктором:
 *     AuthManager(
 *         authRepository: AuthRepository,
 *         authSessionStorage: AuthSessionStorage,
 *     )
 * - поля и методы:
 *     val authState: kotlinx.coroutines.flow.StateFlow<AuthState>
 *     suspend fun initialize()
 *     suspend fun logout()
 *
 * Реализация должна удовлетворять тестам ниже.
 */
class AuthManagerTest {

    private class FakeAuthRepository : AuthRepository {
        var lastRefreshCalled: Boolean = false
        var nextRefreshResult: DomainResult<AuthSession> =
            DomainResult.Failure(DomainError.Unknown())

        override suspend fun login(email: String, password: String): DomainResult<AuthSession> {
            error("Not needed in AuthManager tests")
        }

        override suspend fun register(
            credentials: String,
            email: String,
            password: String,
        ): DomainResult<AuthSession> {
            error("Not needed in AuthManager tests")
        }

        override suspend fun refresh(): DomainResult<AuthSession> {
            lastRefreshCalled = true
            return nextRefreshResult
        }
    }

    private class FakeAuthSessionStorage(
        initialSession: AuthSession?,
    ) : AuthSessionStorage {
        private val _flow = MutableStateFlow(initialSession)
        override val sessionFlow = _flow

        var lastSavedSession: AuthSession? = initialSession
        var clearCalled: Boolean = false

        override suspend fun saveSession(session: AuthSession) {
            lastSavedSession = session
            _flow.value = session
            clearCalled = false
        }

        override suspend fun clearSession() {
            lastSavedSession = null
            _flow.value = null
            clearCalled = true
        }
    }

    @Test
    fun `initialize without stored session sets Unauthenticated and does not call refresh`() = runBlocking {
        val repository = FakeAuthRepository()
        val storage = FakeAuthSessionStorage(initialSession = null)

        val manager = AuthManager(
            authRepository = repository,
            authSessionStorage = storage,
        )

        manager.initialize()

        val state = manager.authState.first()
        assertTrue(state is AuthState.Unauthenticated)
        assertEquals(false, repository.lastRefreshCalled)
    }

    @Test
    fun `initialize with stored session calls refresh and on success sets Authenticated`() = runBlocking {
        val initialSession = AuthSession(
            accessToken = "stored-access",
            refreshToken = "stored-refresh",
        )
        val repository = FakeAuthRepository().apply {
            nextRefreshResult = DomainResult.Success(
                AuthSession(
                    accessToken = "new-access",
                    refreshToken = "new-refresh",
                ),
            )
        }
        val storage = FakeAuthSessionStorage(initialSession = initialSession)

        val manager = AuthManager(
            authRepository = repository,
            authSessionStorage = storage,
        )

        manager.initialize()

        val state = manager.authState.first()
        assertTrue(state is AuthState.Authenticated)
        val authed = state as AuthState.Authenticated
        assertEquals("new-access", authed.session.accessToken)
        assertEquals("new-refresh", authed.session.refreshToken)

        // успешный refresh не должен очищать хранилище
        assertEquals(false, storage.clearCalled)
    }

    @Test
    fun `initialize with stored session and Unauthorized refresh clears storage and sets Unauthenticated`() = runBlocking {
        val initialSession = AuthSession(
            accessToken = "stored-access",
            refreshToken = "stored-refresh",
        )
        val repository = FakeAuthRepository().apply {
            nextRefreshResult = DomainResult.Failure(DomainError.Unauthorized)
        }
        val storage = FakeAuthSessionStorage(initialSession = initialSession)

        val manager = AuthManager(
            authRepository = repository,
            authSessionStorage = storage,
        )

        manager.initialize()

        val state = manager.authState.first()
        assertTrue(state is AuthState.Unauthenticated)
        assertTrue(storage.clearCalled)
        assertEquals(null, storage.lastSavedSession)
    }

    @Test
    fun `logout clears storage and sets Unauthenticated`() = runBlocking {
        val initialSession = AuthSession(
            accessToken = "stored-access",
            refreshToken = "stored-refresh",
        )
        val repository = FakeAuthRepository()
        val storage = FakeAuthSessionStorage(initialSession = initialSession)

        val manager = AuthManager(
            authRepository = repository,
            authSessionStorage = storage,
        )

        // имитируем, что пользователь уже аутентифицирован
        manager.initialize()

        manager.logout()

        val state = manager.authState.first()
        assertTrue(state is AuthState.Unauthenticated)
        assertTrue(storage.clearCalled)
        assertEquals(null, storage.lastSavedSession)
    }
}

