package com.stuf.data.repository

import com.stuf.data.api.UserApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.User
import com.stuf.domain.model.UserId
import com.stuf.domain.repository.CurrentUserRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CurrentUserRepositoryImpl @Inject constructor(
    private val api: UserApi,
) : CurrentUserRepository {

    override suspend fun getCurrentUser(): DomainResult<User> {
        val response = safeCall { api.apiUsersGet() }
        return when (response) {
            is DomainResult.Success -> {
                val body = response.value
                if (body.type != ApiResponseType.success) {
                    return DomainResult.Failure(DomainError.Unknown())
                }
                val dto = body.data ?: return DomainResult.Failure(DomainError.Unknown())
                DomainResult.Success(
                    User(
                        id = UserId(dto.id),
                        credentials = dto.credentials,
                        email = dto.email,
                    ),
                )
            }
            is DomainResult.Failure -> response
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): DomainResult<T> {
        val response = try {
            block()
        } catch (e: IOException) {
            return DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            return DomainResult.Failure(DomainError.Unknown(e))
        }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val body = response.body()
            ?: return DomainResult.Failure(DomainError.Unknown())

        return DomainResult.Success(body)
    }
}
