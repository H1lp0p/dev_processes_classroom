package com.stuf.data.repository

import com.stuf.data.api.AuthApi
import com.stuf.data.auth.AuthSessionStorage
import com.stuf.data.auth.AuthTokenManager
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserChangePassword
import com.stuf.data.model.UserLoginDto
import com.stuf.data.model.UserRegisterDto
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import android.util.Log

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val authSessionStorage: AuthSessionStorage,
    private val authTokenManager: AuthTokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): DomainResult<AuthSession> {
        val dto = UserLoginDto(
            email = email,
            password = password,
        )

        return callAuthEndpoint { api.apiAuthLoginPost(dto) }
    }

    override suspend fun register(
        credentials: String,
        email: String,
        password: String,
    ): DomainResult<AuthSession> {
        val dto = UserRegisterDto(
            email = email,
            password = password,
            credentials = credentials,
        )

        return callAuthEndpoint { api.apiAuthRegisterPost(dto) }
    }

    override suspend fun refresh(): DomainResult<AuthSession> {
        val storedSession = authSessionStorage.sessionFlow.first()
            ?: return DomainResult.Failure(DomainError.Unauthorized)
        return callAuthEndpoint { api.apiAuthRefreshPost(token = storedSession.refreshToken) }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): DomainResult<Unit> {
        val dto =
            UserChangePassword(
                oldPassword = oldPassword,
                newPassword = newPassword,
            )
        val response = try {
            api.apiAuthChangePasswordPost(dto)
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        return if (body.type == ApiResponseType.success) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure(DomainError.Validation(body.message ?: "Не удалось сменить пароль"))
        }
    }

    private suspend fun callAuthEndpoint(
        block: suspend () -> retrofit2.Response<ObjectApiResponse>,
    ): DomainResult<AuthSession> {
        val response = try {
            block()
        } catch (e: IOException) {
            Log.e("AuthRepository", "Network error on auth request", e)
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error on auth request", e)
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            val errorBody = try {
                response.errorBody()?.string() ?: "(no body)"
            } catch (e: IOException) {
                "(failed to read body: ${e.message})"
            }
            Log.e("AuthRepository", "Auth request failed: code=${response.code()}, body=$errorBody")
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        // type=error → Validation c сообщением с бэкенда
        if (body.type != ApiResponseType.success) {
            val message = body.message ?: "Authentication error"
            return DomainResult.Failure(DomainError.Validation(message))
        }

        val session = mapToAuthSession(body)
            ?: return DomainResult.Failure(DomainError.Unknown())

        // успешная аутентификация: сохраняем и применяем токен
        authSessionStorage.saveSession(session)
        authTokenManager.applySession(session)

        return DomainResult.Success(session)
    }

    private fun mapToAuthSession(body: ObjectApiResponse): AuthSession? {
        val data = body.data

        @Suppress("UNCHECKED_CAST")
        val map = data as? Map<*, *> ?: return null

        val access = map["accessToken"] as? String ?: return null
        val refresh = map["refreshToken"] as? String ?: return null

        return AuthSession(
            accessToken = access,
            refreshToken = refresh,
        )
    }
}

