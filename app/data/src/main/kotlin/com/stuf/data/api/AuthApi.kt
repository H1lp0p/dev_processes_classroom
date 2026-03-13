package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.UserChangePassword
import com.stuf.data.model.UserLoginDto
import com.stuf.data.model.UserRegisterDto

interface AuthApi {
    /**
     * POST api/auth/change-password
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param userChangePassword  (optional)
     * @return [ObjectApiResponse]
     */
    @POST("api/auth/change-password")
    suspend fun apiAuthChangePasswordPost(@Body userChangePassword: UserChangePassword? = null): Response<ObjectApiResponse>

    /**
     * POST api/auth/login
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param userLoginDto  (optional)
     * @return [ObjectApiResponse]
     */
    @POST("api/auth/login")
    suspend fun apiAuthLoginPost(@Body userLoginDto: UserLoginDto? = null): Response<ObjectApiResponse>

    /**
     * POST api/auth/logout
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @return [ObjectApiResponse]
     */
    @POST("api/auth/logout")
    suspend fun apiAuthLogoutPost(): Response<ObjectApiResponse>

    /**
     * POST api/auth/refresh
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param token  (optional)
     * @return [ObjectApiResponse]
     */
    @POST("api/auth/refresh")
    suspend fun apiAuthRefreshPost(@Query("token") token: kotlin.String? = null): Response<ObjectApiResponse>

    /**
     * POST api/auth/register
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param userRegisterDto  (optional)
     * @return [ObjectApiResponse]
     */
    @POST("api/auth/register")
    suspend fun apiAuthRegisterPost(@Body userRegisterDto: UserRegisterDto? = null): Response<ObjectApiResponse>

}
