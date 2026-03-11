package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.CourseCourseIdTaskPost200Response
import com.stuf.data.model.CreateCommentRequest
import com.stuf.data.model.PostIdCommentGet200Response
import com.stuf.data.model.UpdateCommentRequest

interface CommentApi {
    /**
     * DELETE comment/{id}
     * Удалить комментарий
     * 
     * Responses:
     *  - 200: Комментарий удален
     *
     * @param id 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @DELETE("comment/{id}")
    suspend fun commentIdDelete(@Path("id") id: java.util.UUID): Response<CourseCourseIdTaskPost200Response>

    /**
     * PUT comment/{id}
     * Редактировать комментарий
     * 
     * Responses:
     *  - 200: Комментарий отредактирован
     *
     * @param id 
     * @param updateCommentRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @PUT("comment/{id}")
    suspend fun commentIdPut(@Path("id") id: java.util.UUID, @Body updateCommentRequest: UpdateCommentRequest): Response<CourseCourseIdTaskPost200Response>

    /**
     * GET comment/{id}/replies
     * Получить ответы на комментарий
     * 
     * Responses:
     *  - 200: Список ответов
     *
     * @param id 
     * @return [PostIdCommentGet200Response]
     */
    @GET("comment/{id}/replies")
    suspend fun commentIdRepliesGet(@Path("id") id: java.util.UUID): Response<PostIdCommentGet200Response>

    /**
     * POST comment/{id}/reply
     * Ответить на комментарий
     * 
     * Responses:
     *  - 200: Ответ создан
     *
     * @param id 
     * @param createCommentRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @POST("comment/{id}/reply")
    suspend fun commentIdReplyPost(@Path("id") id: java.util.UUID, @Body createCommentRequest: CreateCommentRequest): Response<CourseCourseIdTaskPost200Response>

    /**
     * GET post/{id}/comment
     * Получить root-комментарии к посту
     * 
     * Responses:
     *  - 200: Список комментариев
     *
     * @param id 
     * @return [PostIdCommentGet200Response]
     */
    @GET("post/{id}/comment")
    suspend fun postIdCommentGet(@Path("id") id: java.util.UUID): Response<PostIdCommentGet200Response>

    /**
     * POST post/{id}/comment
     * Прокомментировать пост
     * 
     * Responses:
     *  - 200: Комментарий создан
     *
     * @param id 
     * @param createCommentRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @POST("post/{id}/comment")
    suspend fun postIdCommentPost(@Path("id") id: java.util.UUID, @Body createCommentRequest: CreateCommentRequest): Response<CourseCourseIdTaskPost200Response>

    /**
     * GET solution/{id}/comment
     * Получить root-комментарии к решению
     * 
     * Responses:
     *  - 200: Список комментариев
     *
     * @param id 
     * @return [PostIdCommentGet200Response]
     */
    @GET("solution/{id}/comment")
    suspend fun solutionIdCommentGet(@Path("id") id: java.util.UUID): Response<PostIdCommentGet200Response>

    /**
     * POST solution/{id}/comment
     * Прокомментировать решение
     * 
     * Responses:
     *  - 200: Комментарий создан
     *
     * @param id 
     * @param createCommentRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @POST("solution/{id}/comment")
    suspend fun solutionIdCommentPost(@Path("id") id: java.util.UUID, @Body createCommentRequest: CreateCommentRequest): Response<CourseCourseIdTaskPost200Response>

}
