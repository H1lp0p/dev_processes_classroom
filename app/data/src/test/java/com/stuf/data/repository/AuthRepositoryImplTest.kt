package com.stuf.data.repository

import com.stuf.data.api.AuthApi
import com.stuf.data.auth.AuthSessionStorage
import com.stuf.data.auth.AuthTokenManager
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserLoginDto
import com.stuf.data.model.UserRegisterDto
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

private class FakeAuthApi : AuthApi {

    var lastLoginDto: UserLoginDto? = null
    var lastRegisterDto: UserRegisterDto? = null
    var lastRefreshToken: String? = null

    var loginResponse: Response<ObjectApiResponse>? = null
    var registerResponse: Response<ObjectApiResponse>? = null
    var refreshResponse: Response<ObjectApiResponse>? = null

    var throwOnLogin: Throwable? = null

    override suspend fun apiAuthChangePasswordPost(userChangePassword: com.stuf.data.model.UserChangePassword?): Response<ObjectApiResponse> {
        error("Not needed in these tests")
    }

    override suspend fun apiAuthLoginPost(userLoginDto: UserLoginDto?): Response<ObjectApiResponse> {
        throwOnLogin?.let { throw it }
        lastLoginDto = userLoginDto
        return requireNotNull(loginResponse) { "loginResponse not set in FakeAuthApi" }
    }

    override suspend fun apiAuthLogoutPost(): Response<ObjectApiResponse> {
        error("Not needed in these tests")
    }

    override suspend fun apiAuthRefreshPost(token: String?): Response<ObjectApiResponse> {
        lastRefreshToken = token
        return requireNotNull(refreshResponse) { "refreshResponse not set in FakeAuthApi" }
    }

    override suspend fun apiAuthRegisterPost(userRegisterDto: UserRegisterDto?): Response<ObjectApiResponse> {
        lastRegisterDto = userRegisterDto
        return requireNotNull(registerResponse) { "registerResponse not set in FakeAuthApi" }
    }
}

private class FakeAuthSessionStorage : AuthSessionStorage {
    var lastSavedSession: AuthSession? = null
    var cleared: Boolean = false

    override val sessionFlow = kotlinx.coroutines.flow.flow<AuthSession?> {
        emit(lastSavedSession)
    }

    override suspend fun saveSession(session: AuthSession) {
        lastSavedSession = session
        cleared = false
    }

    override suspend fun clearSession() {
        lastSavedSession = null
        cleared = true
    }
}

private class FakeAuthTokenManager : AuthTokenManager(com.stuf.data.auth.BearerTokenApplier { }) {
    var lastAppliedSession: AuthSession? = null

    override fun applySession(session: AuthSession?) {
        lastAppliedSession = session
    }
}

class AuthRepositoryImplTest {

    @Test
    fun `login success maps tokens and returns DomainResult_Success`() {
        val api = FakeAuthApi().apply {
            loginResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.success,
                    data = mapOf(
                        "accessToken" to "access-token",
                        "refreshToken" to "refresh-token",
                    ),
                ),
            )
        }

        val storage = FakeAuthSessionStorage()
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.login(email = "user@example.com", password = "password123")
        }

        assertTrue(result is DomainResult.Success<AuthSession>)
        val session = (result as DomainResult.Success<AuthSession>).value
        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)

        // проверяем, что DTO для логина собран корректно
        assertEquals("user@example.com", api.lastLoginDto?.email)
        assertEquals("password123", api.lastLoginDto?.password)

        // проверяем, что сессия сохранена и применена
        assertEquals(session, storage.lastSavedSession)
        assertEquals(session, tokenManager.lastAppliedSession)
    }

    @Test
    fun `login with backend error type returns DomainResult_Failure_Validation`() {
        val api = FakeAuthApi().apply {
            loginResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.error,
                    message = "Invalid credentials",
                    data = null,
                ),
            )
        }
        val storage = FakeAuthSessionStorage()
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.login(email = "user@example.com", password = "wrong")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Validation)
        assertEquals("Invalid credentials", (error as DomainError.Validation).message)
    }

    @Test
    fun `login http 401 returns DomainResult_Failure_Unauthorized`() {
        val api = FakeAuthApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            loginResponse = Response.error(401, body)
        }
        val storage = FakeAuthSessionStorage()
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.login(email = "user@example.com", password = "wrong")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }

    @Test
    fun `login network exception returns DomainResult_Failure_Network`() {
        val api = FakeAuthApi().apply {
            throwOnLogin = IOException("network down")
        }
        val storage = FakeAuthSessionStorage()
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.login(email = "user@example.com", password = "password123")
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertTrue(error is DomainError.Network)
    }

    @Test
    fun `register success behaves like login success`() {
        val api = FakeAuthApi().apply {
            registerResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.success,
                    data = mapOf(
                        "accessToken" to "access-token",
                        "refreshToken" to "refresh-token",
                    ),
                ),
            )
        }
        val storage = FakeAuthSessionStorage()
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.register(credentials = "User Name", email = "user@example.com", password = "password123")
        }

        assertTrue(result is DomainResult.Success<AuthSession>)
        val session = (result as DomainResult.Success<AuthSession>).value
        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)

        assertEquals("user@example.com", api.lastRegisterDto?.email)
        assertEquals("password123", api.lastRegisterDto?.password)
        assertEquals("User Name", api.lastRegisterDto?.credentials)

        // проверяем, что сессия сохранена и применена
        assertEquals(session, storage.lastSavedSession)
        assertEquals(session, tokenManager.lastAppliedSession)
    }

    @Test
    fun `refresh success maps tokens and returns DomainResult_Success`() {
        val api = FakeAuthApi().apply {
            refreshResponse = Response.success(
                ObjectApiResponse(
                    type = ApiResponseType.success,
                    data = mapOf(
                        "accessToken" to "new-access-token",
                        "refreshToken" to "new-refresh-token",
                    ),
                ),
            )
        }
        val storage = FakeAuthSessionStorage().apply {
            lastSavedSession = AuthSession(
                accessToken = "old-access-token",
                refreshToken = "old-refresh-token",
            )
        }
        val tokenManager = FakeAuthTokenManager()
        val repository : AuthRepository = AuthRepositoryImpl(api, storage, tokenManager)

        val result = runBlocking {
            repository.refresh()
        }

        assertTrue(result is DomainResult.Success<AuthSession>)
        val session = (result as DomainResult.Success<AuthSession>).value
        assertEquals("new-access-token", session.accessToken)
        assertEquals("new-refresh-token", session.refreshToken)

        assertEquals("old-refresh-token", api.lastRefreshToken)

        // при успешном refresh новая сессия также сохраняется и применяется
        assertEquals(session, storage.lastSavedSession)
        assertEquals(session, tokenManager.lastAppliedSession)
    }
}

