package com.stuf.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Контракт локального хранилища auth-сессии (access/refresh токены).
 */
interface AuthSessionStorage {
    val sessionFlow: Flow<AuthSession?>

    suspend fun saveSession(session: AuthSession)

    suspend fun clearSession()
}

/**
 * Реализация на базе DataStore<Preferences>.
 *
 * Ключи довольно простые и завязаны только на этот модуль.
 */
class AuthSessionStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : AuthSessionStorage {

    private val accessKey = stringPreferencesKey("auth_access_token")
    private val refreshKey = stringPreferencesKey("auth_refresh_token")

    override val sessionFlow: Flow<AuthSession?> =
        dataStore.data.map { prefs ->
            val access = prefs[accessKey]
            val refresh = prefs[refreshKey]
            if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                null
            } else {
                AuthSession(
                    accessToken = access,
                    refreshToken = refresh,
                )
            }
        }

    override suspend fun saveSession(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[accessKey] = session.accessToken
            prefs[refreshKey] = session.refreshToken
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(accessKey)
            prefs.remove(refreshKey)
        }
    }
}

