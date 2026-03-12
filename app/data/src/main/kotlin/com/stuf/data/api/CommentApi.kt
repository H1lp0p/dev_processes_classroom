package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.AddCommentRequestDto
import com.stuf.data.model.CommentDtoListApiResponse
import com.stuf.data.model.EditCommentRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse

interface CommentApi {
    /**
     * DELETE api/comment/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [IdRequestDtoApiResponse]
     */
    @DELETE("api/comment/{id}")
    suspend fun apiCommentIdDelete(@Path("id") id: java.util.UUID): Response<IdRequestDtoApiResponse>

    /**
     * PUT api/comment/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param editCommentRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @PUT("api/comment/{id}")
    suspend fun apiCommentIdPut(@Path("id") id: java.util.UUID, @Body editCommentRequestDto: EditCommentRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * GET api/comment/{id}/replies
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [CommentDtoListApiResponse]
     */
    @GET("api/comment/{id}/replies")
    suspend fun apiCommentIdRepliesGet(@Path("id") id: java.util.UUID): Response<CommentDtoListApiResponse>

    /**
     * POST api/comment/{id}/reply
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param addCommentRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/comment/{id}/reply")
    suspend fun apiCommentIdReplyPost(@Path("id") id: java.util.UUID, @Body addCommentRequestDto: AddCommentRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * GET api/post/{id}/comment
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [CommentDtoListApiResponse]
     */
    @GET("api/post/{id}/comment")
    suspend fun apiPostIdCommentGet(@Path("id") id: java.util.UUID): Response<CommentDtoListApiResponse>

    /**
     * POST api/post/{id}/comment
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param addCommentRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/post/{id}/comment")
    suspend fun apiPostIdCommentPost(@Path("id") id: java.util.UUID, @Body addCommentRequestDto: AddCommentRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * GET api/solution/{id}/comment
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [CommentDtoListApiResponse]
     */
    @GET("api/solution/{id}/comment")
    suspend fun apiSolutionIdCommentGet(@Path("id") id: java.util.UUID): Response<CommentDtoListApiResponse>

    /**
     * POST api/solution/{id}/comment
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param addCommentRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/solution/{id}/comment")
    suspend fun apiSolutionIdCommentPost(@Path("id") id: java.util.UUID, @Body addCommentRequestDto: AddCommentRequestDto? = null): Response<IdRequestDtoApiResponse>

}
