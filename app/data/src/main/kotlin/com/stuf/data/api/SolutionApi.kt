package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.CourseCourseIdTaskPost200Response
import com.stuf.data.model.ReviewRequest
import com.stuf.data.model.SubmitSolutionRequest
import com.stuf.data.model.TaskIdSolutionGet200Response
import com.stuf.data.model.TaskIdSolutionsGet200Response

interface SolutionApi {
    /**
     * POST solution/{solutionId}/review
     * Проверить решение
     * 
     * Responses:
     *  - 200: Решение проверено
     *
     * @param solutionId 
     * @param reviewRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @POST("solution/{solutionId}/review")
    suspend fun solutionSolutionIdReviewPost(@Path("solutionId") solutionId: java.util.UUID, @Body reviewRequest: ReviewRequest): Response<CourseCourseIdTaskPost200Response>

    /**
     * DELETE task/{id}/solution
     * Удалить решение
     * 
     * Responses:
     *  - 200: Решение удалено
     *
     * @param id 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @DELETE("task/{id}/solution")
    suspend fun taskIdSolutionDelete(@Path("id") id: java.util.UUID): Response<CourseCourseIdTaskPost200Response>

    /**
     * GET task/{id}/solution
     * Получить своё решение по заданию
     * 
     * Responses:
     *  - 200: Решение пользователя
     *
     * @param id 
     * @return [TaskIdSolutionGet200Response]
     */
    @GET("task/{id}/solution")
    suspend fun taskIdSolutionGet(@Path("id") id: java.util.UUID): Response<TaskIdSolutionGet200Response>

    /**
     * PUT task/{id}/solution
     * Отправить или отредактировать решение
     * 
     * Responses:
     *  - 200: Решение отправлено
     *
     * @param id 
     * @param submitSolutionRequest 
     * @return [CourseCourseIdTaskPost200Response]
     */
    @PUT("task/{id}/solution")
    suspend fun taskIdSolutionPut(@Path("id") id: java.util.UUID, @Body submitSolutionRequest: SubmitSolutionRequest): Response<CourseCourseIdTaskPost200Response>


    /**
    * enum for parameter status
    */
    enum class StatusTaskIdSolutionsGet(val value: kotlin.String) {
        @Json(name = "pending") pending("pending"),
        @Json(name = "checked") checked("checked"),
        @Json(name = "returned") returned("returned")
    }

    /**
     * GET task/{id}/solutions
     * Получить список решений по заданию
     * 
     * Responses:
     *  - 200: Список решений
     *
     * @param id 
     * @param skip Сколько записей пропустить (optional, default to 0)
     * @param take Сколько записей взять после пропуска (optional, default to 10)
     * @param status Фильтр по статусу решения (optional)
     * @param studentId Фильтр по конкретному ученику (optional)
     * @return [TaskIdSolutionsGet200Response]
     */
    @GET("task/{id}/solutions")
    suspend fun taskIdSolutionsGet(@Path("id") id: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 10, @Query("status") status: StatusTaskIdSolutionsGet? = null, @Query("studentId") studentId: java.util.UUID? = null): Response<TaskIdSolutionsGet200Response>

}
