package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.data.model.SolutionListDtoApiResponse
import com.stuf.data.model.SolutionStatus
import com.stuf.data.model.StudentSolutionDetailsDtoApiResponse
import com.stuf.data.model.SubmitSolutionRequestDto
import com.stuf.data.model.UpdateSolutionRequestDto

interface SolutionApi {
    /**
     * POST api/solution/{solutionId}/review
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param solutionId 
     * @param updateSolutionRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/solution/{solutionId}/review")
    suspend fun apiSolutionSolutionIdReviewPost(@Path("solutionId") solutionId: java.util.UUID, @Body updateSolutionRequestDto: UpdateSolutionRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * DELETE api/task/{id}/solution
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
    @DELETE("api/task/{id}/solution")
    suspend fun apiTaskIdSolutionDelete(@Path("id") id: java.util.UUID): Response<IdRequestDtoApiResponse>

    /**
     * GET api/task/{id}/solution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @return [StudentSolutionDetailsDtoApiResponse]
     */
    @GET("api/task/{id}/solution")
    suspend fun apiTaskIdSolutionGet(@Path("id") id: java.util.UUID): Response<StudentSolutionDetailsDtoApiResponse>

    /**
     * PUT api/task/{id}/solution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param submitSolutionRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @PUT("api/task/{id}/solution")
    suspend fun apiTaskIdSolutionPut(@Path("id") id: java.util.UUID, @Body submitSolutionRequestDto: SubmitSolutionRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * GET api/task/{id}/solutions
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param id 
     * @param skip  (optional, default to 0)
     * @param take  (optional, default to 20)
     * @param status  (optional)
     * @param studentId  (optional)
     * @return [SolutionListDtoApiResponse]
     */
    @GET("api/task/{id}/solutions")
    suspend fun apiTaskIdSolutionsGet(@Path("id") id: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 20, @Query("status") status: SolutionStatus? = null, @Query("studentId") studentId: java.util.UUID? = null): Response<SolutionListDtoApiResponse>

}
