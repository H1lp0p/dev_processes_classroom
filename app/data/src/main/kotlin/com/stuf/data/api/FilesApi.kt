package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.IdRequestDtoApiResponse

import okhttp3.MultipartBody

interface FilesApi {
    /**
     * GET api/files/{id}
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param id 
     * @return [Unit]
     */
    @GET("api/files/{id}")
    suspend fun apiFilesIdGet(@Path("id") id: java.util.UUID): Response<Unit>

    /**
     * POST api/files/upload
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param file  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @Multipart
    @POST("api/files/upload")
    suspend fun apiFilesUploadPost(@Part file: MultipartBody.Part? = null): Response<IdRequestDtoApiResponse>

}
