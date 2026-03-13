package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.CreateUpdatePostDto
import com.stuf.data.model.FeedResponseDtoApiResponse
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.data.model.PostDetailsDtoApiResponse

interface PostApi {
    /**
     * GET api/course/{courseId}/feed
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param courseId 
     * @param skip  (optional, default to 0)
     * @param take  (optional, default to 20)
     * @return [FeedResponseDtoApiResponse]
     */
    @GET("api/course/{courseId}/feed")
    suspend fun apiCourseCourseIdFeedGet(@Path("courseId") courseId: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 20): Response<FeedResponseDtoApiResponse>

    /**
     * POST api/course/{courseId}/task
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param courseId 
     * @param createUpdatePostDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/course/{courseId}/task")
    suspend fun apiCourseCourseIdTaskPost(@Path("courseId") courseId: java.util.UUID, @Body createUpdatePostDto: CreateUpdatePostDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * DELETE api/post/{id}
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
    @DELETE("api/post/{id}")
    suspend fun apiPostIdDelete(@Path("id") id: java.util.UUID): Response<IdRequestDtoApiResponse>

    /**
     * GET api/post/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [PostDetailsDtoApiResponse]
     */
    @GET("api/post/{id}")
    suspend fun apiPostIdGet(@Path("id") id: java.util.UUID): Response<PostDetailsDtoApiResponse>

    /**
     * PUT api/post/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param createUpdatePostDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @PUT("api/post/{id}")
    suspend fun apiPostIdPut(@Path("id") id: java.util.UUID, @Body createUpdatePostDto: CreateUpdatePostDto? = null): Response<IdRequestDtoApiResponse>

}
