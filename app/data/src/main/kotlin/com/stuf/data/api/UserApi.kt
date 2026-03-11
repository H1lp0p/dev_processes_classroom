package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.ResponseWrapper
import com.stuf.data.model.UpdateUserRequest
import com.stuf.data.model.UserCoursesGet200Response
import com.stuf.data.model.UsersGet200Response

interface UserApi {
    /**
     * GET user/courses
     * Получить список курсов пользователя
     * 
     * Responses:
     *  - 200: Список курсов пользователя
     *
     * @return [UserCoursesGet200Response]
     */
    @GET("user/courses")
    suspend fun userCoursesGet(): Response<UserCoursesGet200Response>

    /**
     * GET users
     * Получить данные о себе
     * 
     * Responses:
     *  - 200: Данные пользователя
     *
     * @return [UsersGet200Response]
     */
    @GET("users")
    suspend fun usersGet(): Response<UsersGet200Response>

    /**
     * GET users/{id}
     * Получить данные о пользователе
     * 
     * Responses:
     *  - 200: Данные пользователя
     *
     * @param id 
     * @return [UsersGet200Response]
     */
    @GET("users/{id}")
    suspend fun usersIdGet(@Path("id") id: java.util.UUID): Response<UsersGet200Response>

    /**
     * PUT users
     * Обновить данные о себе
     * 
     * Responses:
     *  - 200: Данные успешно обновлены
     *
     * @param updateUserRequest 
     * @return [ResponseWrapper]
     */
    @PUT("users")
    suspend fun usersPut(@Body updateUserRequest: UpdateUserRequest): Response<ResponseWrapper>

}
