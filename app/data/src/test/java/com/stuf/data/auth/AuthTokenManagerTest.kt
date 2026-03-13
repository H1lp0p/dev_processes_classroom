package com.stuf.data.auth

import com.stuf.domain.repository.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TDD-спека для управления bearer-токеном на уровне data-модуля.
 *
 * Задаёт контракт:
 * - интерфейс BearerTokenApplier с методом setBearerToken(token: String)
 * - класс AuthTokenManager с конструктором (applier: BearerTokenApplier)
 *   и методом applySession(session: AuthSession?)
 *
 * В боевой реализации BearerTokenApplier будет адаптером над ApiClient.setBearerToken,
 * здесь же мы проверяем только корректные вызовы applier'а.
 */
class AuthTokenManagerTest {

    private class FakeBearerTokenApplier : BearerTokenApplier {
        var lastToken: String? = null

        override fun setBearerToken(token: String) {
            lastToken = token
        }
    }

    @Test
    fun `applySession with non-null session sets access token via applier`() {
        val applier = FakeBearerTokenApplier()
        val manager = AuthTokenManager(applier)

        val session = AuthSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        manager.applySession(session)

        assertEquals("access-token", applier.lastToken)
    }

    @Test
    fun `applySession with null session clears bearer token`() {
        val applier = FakeBearerTokenApplier()
        val manager = AuthTokenManager(applier)

        manager.applySession(null)

        assertEquals("", applier.lastToken)
    }
}

