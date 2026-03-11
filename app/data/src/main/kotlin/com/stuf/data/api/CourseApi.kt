package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.CourseIdFeedGet200Response
import com.stuf.data.model.CourseIdGet200Response
import com.stuf.data.model.CourseIdLeaveDelete200Response
import com.stuf.data.model.CourseIdMembersGet200Response
import com.stuf.data.model.CourseIdMembersUserIdRolePut200Response
import com.stuf.data.model.CourseJoinPost200Response
import com.stuf.data.model.CoursePost200Response
import com.stuf.data.model.CreateCourseRequest
import com.stuf.data.model.JoinCourseRequest
import com.stuf.data.model.UpdateCourseRequest
import com.stuf.data.model.UpdateRoleRequest

interface CourseApi {
    /**
     * GET course/{id}/feed
     * Получить посты курса
     * 
     * Responses:
     *  - 200: Список постов курса
     *
     * @param id 
     * @param skip Сколько записей пропустить (optional, default to 0)
     * @param take Сколько записей взять после пропуска (optional, default to 10)
     * @return [CourseIdFeedGet200Response]
     */
    @GET("course/{id}/feed")
    suspend fun courseIdFeedGet(@Path("id") id: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 10): Response<CourseIdFeedGet200Response>

    /**
     * GET course/{id}
     * Получить информацию о курсе
     * 
     * Responses:
     *  - 200: Информация о курсе
     *
     * @param id 
     * @return [CourseIdGet200Response]
     */
    @GET("course/{id}")
    suspend fun courseIdGet(@Path("id") id: java.util.UUID): Response<CourseIdGet200Response>

    /**
     * DELETE course/{id}/leave
     * Покинуть курс
     * 
     * Responses:
     *  - 200: Курс покинут
     *
     * @param id 
     * @return [CourseIdLeaveDelete200Response]
     */
    @DELETE("course/{id}/leave")
    suspend fun courseIdLeaveDelete(@Path("id") id: java.util.UUID): Response<CourseIdLeaveDelete200Response>

    /**
     * GET course/{id}/members
     * Получить список участников курса
     * 
     * Responses:
     *  - 200: Список участников курса
     *
     * @param id 
     * @param skip Сколько записей пропустить (optional, default to 0)
     * @param take Сколько записей взять после пропуска (optional, default to 10)
     * @param query Поисковый запрос для фильтрации (optional)
     * @return [CourseIdMembersGet200Response]
     */
    @GET("course/{id}/members")
    suspend fun courseIdMembersGet(@Path("id") id: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 10, @Query("query") query: kotlin.String? = null): Response<CourseIdMembersGet200Response>

    /**
     * DELETE course/{id}/members/{userId}
     * Удалить участника из курса
     * 
     * Responses:
     *  - 200: Участник удален
     *
     * @param id 
     * @param userId 
     * @return [CourseIdLeaveDelete200Response]
     */
    @DELETE("course/{id}/members/{userId}")
    suspend fun courseIdMembersUserIdDelete(@Path("id") id: java.util.UUID, @Path("userId") userId: java.util.UUID): Response<CourseIdLeaveDelete200Response>

    /**
     * PUT course/{id}/members/{userId}/role
     * Изменить роль участника
     * 
     * Responses:
     *  - 200: Роль изменена
     *
     * @param id 
     * @param userId 
     * @param updateRoleRequest 
     * @return [CourseIdMembersUserIdRolePut200Response]
     */
    @PUT("course/{id}/members/{userId}/role")
    suspend fun courseIdMembersUserIdRolePut(@Path("id") id: java.util.UUID, @Path("userId") userId: java.util.UUID, @Body updateRoleRequest: UpdateRoleRequest): Response<CourseIdMembersUserIdRolePut200Response>

    /**
     * PUT course/{id}
     * Изменить курс
     * 
     * Responses:
     *  - 200: Курс изменен
     *
     * @param id 
     * @param updateCourseRequest 
     * @return [CoursePost200Response]
     */
    @PUT("course/{id}")
    suspend fun courseIdPut(@Path("id") id: java.util.UUID, @Body updateCourseRequest: UpdateCourseRequest): Response<CoursePost200Response>

    /**
     * POST course/join
     * Присоединиться к курсу по коду
     * 
     * Responses:
     *  - 200: Успешное присоединение к курсу
     *
     * @param joinCourseRequest 
     * @return [CourseJoinPost200Response]
     */
    @POST("course/join")
    suspend fun courseJoinPost(@Body joinCourseRequest: JoinCourseRequest): Response<CourseJoinPost200Response>

    /**
     * POST course
     * Создать курс
     * 
     * Responses:
     *  - 200: Курс создан
     *
     * @param createCourseRequest 
     * @return [CoursePost200Response]
     */
    @POST("course")
    suspend fun coursePost(@Body createCourseRequest: CreateCourseRequest): Response<CoursePost200Response>

}
