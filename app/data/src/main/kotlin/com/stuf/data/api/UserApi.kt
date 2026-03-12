package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserDtoApiResponse
import com.stuf.data.model.UserDtoListApiResponse
import com.stuf.data.model.UserUpdateDto

interface UserApi {
    /**
     * GET api/users
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @return [UserDtoApiResponse]
     */
    @GET("api/users")
    suspend fun apiUsersGet(): Response<UserDtoApiResponse>

    /**
     * GET api/users/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [UserDtoApiResponse]
     */
    @GET("api/users/{id}")
    suspend fun apiUsersIdGet(@Path("id") id: java.util.UUID): Response<UserDtoApiResponse>

    /**
     * PUT api/users
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param userUpdateDto  (optional)
     * @return [ObjectApiResponse]
     */
    @PUT("api/users")
    suspend fun apiUsersPut(@Body userUpdateDto: UserUpdateDto? = null): Response<ObjectApiResponse>

    /**
     * GET api/users/search
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param query  (optional)
     * @return [UserDtoListApiResponse]
     */
    @GET("api/users/search")
    suspend fun apiUsersSearchGet(@Query("query") query: kotlin.String? = null): Response<UserDtoListApiResponse>

}
