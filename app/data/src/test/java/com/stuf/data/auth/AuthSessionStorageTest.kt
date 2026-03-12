package com.stuf.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID
import java.util.prefs.Preferences

/**
 * TDD-спека для локального хранения AuthSession.
 *
 * Задаёт контракт для:
 * - интерфейса AuthSessionStorage;
 * - реализации AuthSessionStorageImpl с конструктором (dataStore: DataStore<Preferences>).
 *
 * Реализация должна удовлетворять этим тестам, при этом внутренняя реализация
 * может основываться на DataStore.
 */
class AuthSessionStorageTest {

    private fun createTestDataStore(): DataStore<Preferences> {
        val fileName = "auth_session_test_${UUID.randomUUID()}.preferences_pb"
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { fileName.toPath() },
        )
    }

    private fun createStorage(): AuthSessionStorage {
        val dataStore = createTestDataStore()
        // Контракт конструктора реализации:
        // class AuthSessionStorageImpl(
        //     private val dataStore: DataStore<Preferences>,
        // ) : AuthSessionStorage { ... }
        return AuthSessionStorageImpl(dataStore)
    }

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

