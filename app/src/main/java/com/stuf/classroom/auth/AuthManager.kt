package com.stuf.classroom.auth

import com.stuf.data.auth.AuthSessionStorage
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Контракт глобального менеджера авторизации.
 *
 * Используется ViewModel'ями логина/регистрации и верхнеуровневым UI.
 */
interface AuthManager {
    val authState: StateFlow<AuthState>

    suspend fun initialize()

    suspend fun logout()

    /**
     * Вызывается, когда login/register успешно завершились и вернули сессию.
     * Здесь можно синхронизировать локальное состояние UI с уже сохранённой
     * в data-слое сессией.
     */
    fun onAuthSuccess(session: AuthSession)
}

/**
 * Реализация, опирающаяся на AuthRepository и AuthSessionStorage.
 *
 * - При старте (initialize) читает сохранённую сессию и, если она есть,
 *   выполняет refresh. Только успешный refresh приводит к Authenticated.
 * - При logout очищает хранилище и переводит состояние в Unauthenticated.
 */
class DefaultAuthManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val authSessionStorage: AuthSessionStorage,
) : AuthManager {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun initialize() {
        val storedSession = authSessionStorage.sessionFlow.first()
        if (storedSession == null) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        _authState.value = AuthState.Loading

        val result = withContext(kotlinx.coroutines.Dispatchers.IO) {
            authRepository.refresh()
        }

        when (result) {
            is DomainResult.Success -> {
                _authState.value = AuthState.Authenticated(result.value)
            }

            is DomainResult.Failure -> {
                if (result.error == DomainError.Unauthorized) {
                    authSessionStorage.clearSession()
                }
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun logout() {
        authSessionStorage.clearSession()
        _authState.value = AuthState.Unauthenticated
    }

    override fun onAuthSuccess(session: AuthSession) {
        // Repository уже сохранил и применил токен; здесь просто обновляем
        // локальное состояние приложения.
        _authState.value = AuthState.Authenticated(session)
        // При желании можно также запланировать refresh позже и т.п.
    }
}

