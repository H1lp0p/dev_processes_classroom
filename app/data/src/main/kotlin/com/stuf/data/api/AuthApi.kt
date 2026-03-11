package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.AuthRegisterPost200Response
import com.stuf.data.model.LoginRequest
import com.stuf.data.model.RegisterRequest
import com.stuf.data.model.ResponseWrapper

interface AuthApi {
    /**
     * POST auth/login
     * Вход в систему
     * 
     * Responses:
     *  - 200: Успешный вход
     *
     * @param loginRequest 
     * @return [AuthRegisterPost200Response]
     */
    @POST("auth/login")
    suspend fun authLoginPost(@Body loginRequest: LoginRequest): Response<AuthRegisterPost200Response>

    /**
     * POST auth/logout
     * Выход из системы
     * 
     * Responses:
     *  - 200: Успешный выход
     *
     * @return [ResponseWrapper]
     */
    @POST("auth/logout")
    suspend fun authLogoutPost(): Response<ResponseWrapper>

    /**
     * POST auth/refresh
     * Обновление токена
     * 
     * Responses:
     *  - 200: Токен успешно обновлен
     *
     * @return [AuthRegisterPost200Response]
     */
    @POST("auth/refresh")
    suspend fun authRefreshPost(): Response<AuthRegisterPost200Response>

    /**
     * POST auth/register
     * Регистрация нового пользователя
     * 
     * Responses:
     *  - 200: Успешная регистрация
     *
     * @param registerRequest 
     * @return [AuthRegisterPost200Response]
     */
    @POST("auth/register")
    suspend fun authRegisterPost(@Body registerRequest: RegisterRequest): Response<AuthRegisterPost200Response>

}
