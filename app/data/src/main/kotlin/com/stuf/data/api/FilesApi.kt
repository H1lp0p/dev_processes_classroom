package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import com.squareup.moshi.Json

import com.stuf.data.model.FilesUploadPost200Response
import com.stuf.data.model.ResponseWrapper

import okhttp3.MultipartBody

interface FilesApi {
    /**
     * GET files/{id}
     * Получить файл
     * 
     * Responses:
     *  - 200: Файл
     *  - 403: Ошибка доступа
     *  - 404: Файл не найден
     *
     * @param id 
     * @return [ResponseBody]
     */
    @GET("files/{id}")
    suspend fun filesIdGet(@Path("id") id: java.util.UUID): Response<ResponseBody>

    /**
     * POST files/upload
     * Загрузить файл
     * 
     * Responses:
     *  - 200: Файл успешно загружен
     *
     * @param file Файл для загрузки (optional)
     * @return [FilesUploadPost200Response]
     */
    @Multipart
    @POST("files/upload")
    suspend fun filesUploadPost(@Part file: MultipartBody.Part? = null): Response<FilesUploadPost200Response>

}
