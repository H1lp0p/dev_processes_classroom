package com.stuf.data.auth

import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * TDD-спека для локального хранения AuthSession.
 *
 * Задаёт контракт для:
 * - интерфейса AuthSessionStorage;
 * - требований к поведению реализаций (persist/clear/override).
 *
 * Для юнит-тестов здесь используется простая in-memory реализация, чтобы
 * избежать ошибок файловой системы и сфокусироваться на контракте.
 */
class AuthSessionStorageTest {

    private class InMemoryAuthSessionStorage : AuthSessionStorage {
        private val _flow = MutableStateFlow<AuthSession?>(null)
        override val sessionFlow = _flow

        override suspend fun saveSession(session: AuthSession) {
            _flow.value = session
        }

        override suspend fun clearSession() {
            _flow.value = null
        }
    }

    private fun createStorage(): AuthSessionStorage = InMemoryAuthSessionStorage()

    @Test
    fun `initial session is null when nothing saved`() = runBlocking {
        val storage = createStorage()

        val session = storage.sessionFlow.first()

        assertNull(session)
    }

    @Test
    fun `saveSession persists tokens and can be read back`() = runBlocking {
        val storage = createStorage()
        val session = AuthSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        storage.saveSession(session)
        val loaded = storage.sessionFlow.first()

        assertEquals("access-token", loaded?.accessToken)
        assertEquals("refresh-token", loaded?.refreshToken)
    }

    @Test
    fun `saveSession overrides previous value`() = runBlocking {
        val storage = createStorage()

        val first = AuthSession(
            accessToken = "first-access",
            refreshToken = "first-refresh",
        )
        val second = AuthSession(
            accessToken = "second-access",
            refreshToken = "second-refresh",
        )

        storage.saveSession(first)
        storage.saveSession(second)

        val loaded = storage.sessionFlow.first()
        assertEquals("second-access", loaded?.accessToken)
        assertEquals("second-refresh", loaded?.refreshToken)
    }

    @Test
    fun `clearSession removes stored session and sessionFlow emits null`() = runBlocking {
        val storage = createStorage()
        val session = AuthSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        storage.saveSession(session)
        storage.clearSession()

        val loaded = storage.sessionFlow.first()
        assertNull(loaded)
    }
}

