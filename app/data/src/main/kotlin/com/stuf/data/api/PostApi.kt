package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.CourseCourseIdTaskPost200Response
import com.stuf.data.model.CreatePostRequest
import com.stuf.data.model.PostIdGet200Response

interface PostApi {
    /**
     * POST course/{courseId}/task
     * Создать пост в курсе
     * 
     * Responses:
     *  - 200: Пост создан
     *
     * @param courseId 
     * @param createPostRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @POST("course/{courseId}/task")
    suspend fun courseCourseIdTaskPost(@Path("courseId") courseId: java.util.UUID, @Body createPostRequest: CreatePostRequest): Response<CourseCourseIdTaskPost200Response>

    /**
     * DELETE post/{id}
     * Удалить пост
     * 
     * Responses:
     *  - 200: Пост удален
     *
     * @param id 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @DELETE("post/{id}")
    suspend fun postIdDelete(@Path("id") id: java.util.UUID): Response<CourseCourseIdTaskPost200Response>

    /**
     * GET post/{id}
     * Получить пост
     * 
     * Responses:
     *  - 200: Информация о посте
     *
     * @param id 
     * @return [PostIdGet200Response]
     */
    @GET("post/{id}")
    suspend fun postIdGet(@Path("id") id: java.util.UUID): Response<PostIdGet200Response>

    /**
     * PUT post/{id}
     * Редактировать пост
     * 
     * Responses:
     *  - 200: Пост отредактирован
     *
     * @param id 
     * @param createPostRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @PUT("post/{id}")
    suspend fun postIdPut(@Path("id") id: java.util.UUID, @Body createPostRequest: CreatePostRequest): Response<CourseCourseIdTaskPost200Response>

}
