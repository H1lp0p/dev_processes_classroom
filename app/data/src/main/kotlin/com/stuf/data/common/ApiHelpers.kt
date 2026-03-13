package com.stuf.data.common

import com.stuf.data.model.ApiResponseType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import retrofit2.Response
import java.io.IOException

inline fun <T, R> Response<T>.toDomainResult(
    crossinline mapSuccess: (T) -> R,
): DomainResult<R> {
    return try {
        if (isSuccessful) {
            val body = body()
            if (body != null) {
                DomainResult.Success(mapSuccess(body))
            } else {
                DomainResult.Failure(DomainError.Unknown())
            }
        } else {
            DomainResult.Failure(httpCodeToDomainError(code()))
        }
    } catch (e: IOException) {
        DomainResult.Failure(DomainError.Network(e))
    } catch (e: Exception) {
        DomainResult.Failure(DomainError.Unknown(e))
    }
}

fun httpCodeToDomainError(code: Int): DomainError =
    when (code) {
        401 -> DomainError.Unauthorized
        403 -> DomainError.Forbidden
        404 -> DomainError.NotFound
        in 400..499 -> DomainError.Validation("Request failed with code $code")
        in 500..599 -> DomainError.Unknown()
        else -> DomainError.Unknown()
    }

inline fun <ApiResponse, Data, R> Response<ApiResponse>.toDomainFromWrapped(
    crossinline extract: (ApiResponse) -> Pair<ApiResponseType, Data?>,
    crossinline mapData: (Data) -> R,
): DomainResult<R> {
    return try {
        if (!isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(code()))
        }

        val body = body() ?: return DomainResult.Failure(DomainError.Unknown())
        val (type, data) = extract(body)

        if (type != ApiResponseType.success || data == null) {
            return DomainResult.Failure(
                DomainError.Validation("ApiResponse type=${type} or data is null"),
            )
        }

        DomainResult.Success(mapData(data))
    } catch (e: IOException) {
        DomainResult.Failure(DomainError.Network(e))
    } catch (e: Exception) {
        DomainResult.Failure(DomainError.Unknown(e))
    }
}

