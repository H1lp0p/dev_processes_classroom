package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.ChangeRoleRequestDto
import com.stuf.data.model.ChangeRoleResponseDtoApiResponse
import com.stuf.data.model.CourseDetailsDtoApiResponse
import com.stuf.data.model.CourseMemberDtoPagedResponseApiResponse
import com.stuf.data.model.CreateUpdateCourseRequestDto
import com.stuf.data.model.CreateUpdateCourseResponseDtoApiResponse
import com.stuf.data.model.JoinCourseRequestDto
import com.stuf.data.model.JoinCourseResponseDtoApiResponse
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserCourseDtoPagedResponseApiResponse

interface CourseApi {
    /**
     * GET api/course/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [CourseDetailsDtoApiResponse]
     */
    @GET("api/course/{id}")
    suspend fun apiCourseIdGet(@Path("id") id: java.util.UUID): Response<CourseDetailsDtoApiResponse>

    /**
     * DELETE api/course/{id}/leave
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [ObjectApiResponse]
     */
    @DELETE("api/course/{id}/leave")
    suspend fun apiCourseIdLeaveDelete(@Path("id") id: java.util.UUID): Response<ObjectApiResponse>

    /**
     * GET api/course/{id}/members
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param skip  (optional, default to 0)
     * @param take  (optional, default to 10)
     * @param query  (optional)
     * @return [CourseMemberDtoPagedResponseApiResponse]
     */
    @GET("api/course/{id}/members")
    suspend fun apiCourseIdMembersGet(@Path("id") id: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 10, @Query("query") query: kotlin.String? = null): Response<CourseMemberDtoPagedResponseApiResponse>

    /**
     * DELETE api/course/{id}/members/{userId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param userId 
     * @return [ObjectApiResponse]
     */
    @DELETE("api/course/{id}/members/{userId}")
    suspend fun apiCourseIdMembersUserIdDelete(@Path("id") id: java.util.UUID, @Path("userId") userId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * PUT api/course/{id}/members/{userId}/role
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param userId 
     * @param changeRoleRequestDto  (optional)
     * @return [ChangeRoleResponseDtoApiResponse]
     */
    @PUT("api/course/{id}/members/{userId}/role")
    suspend fun apiCourseIdMembersUserIdRolePut(@Path("id") id: java.util.UUID, @Path("userId") userId: java.util.UUID, @Body changeRoleRequestDto: ChangeRoleRequestDto? = null): Response<ChangeRoleResponseDtoApiResponse>

    /**
     * PUT api/course/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param createUpdateCourseRequestDto  (optional)
     * @return [CreateUpdateCourseResponseDtoApiResponse]
     */
    @PUT("api/course/{id}")
    suspend fun apiCourseIdPut(@Path("id") id: java.util.UUID, @Body createUpdateCourseRequestDto: CreateUpdateCourseRequestDto? = null): Response<CreateUpdateCourseResponseDtoApiResponse>

    /**
     * POST api/course/join
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param joinCourseRequestDto  (optional)
     * @return [JoinCourseResponseDtoApiResponse]
     */
    @POST("api/course/join")
    suspend fun apiCourseJoinPost(@Body joinCourseRequestDto: JoinCourseRequestDto? = null): Response<JoinCourseResponseDtoApiResponse>

    /**
     * POST api/course
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param createUpdateCourseRequestDto  (optional)
     * @return [CreateUpdateCourseResponseDtoApiResponse]
     */
    @POST("api/course")
    suspend fun apiCoursePost(@Body createUpdateCourseRequestDto: CreateUpdateCourseRequestDto? = null): Response<CreateUpdateCourseResponseDtoApiResponse>

    /**
     * GET api/user/courses
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param skip  (optional, default to 0)
     * @param take  (optional, default to 20)
     * @return [UserCourseDtoPagedResponseApiResponse]
     */
    @GET("api/user/courses")
    suspend fun apiUserCoursesGet(@Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 20): Response<UserCourseDtoPagedResponseApiResponse>

}
