package com.stuf.data.auth

import com.stuf.domain.repository.AuthSession

/**
 * Функциональный интерфейс, который знает, как применить bearer-токен
 * к сетевому слою (например, через ApiClient.setBearerToken()).
 */
fun interface BearerTokenApplier {
    fun setBearerToken(token: String)
}

/**
 * Небольшой слой, который конвертирует AuthSession в конкретное значение
 * bearer-токена и передаёт его в BearerTokenApplier.
 */
open class AuthTokenManager(
    private val applier: BearerTokenApplier,
) {

    open fun applySession(session: AuthSession?) {
        val token = session?.accessToken.orEmpty()
        applier.setBearerToken(token)
    }
}

