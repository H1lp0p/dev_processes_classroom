package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.data.model.SolutionStatus
import com.stuf.data.model.StudentTeamSolutionDetailsDtoApiResponse
import com.stuf.data.model.SubmitTeamSolutionRequestDto
import com.stuf.data.model.TeamSolutionListDtoApiResponse
import com.stuf.data.model.UpdateTeamSolutionRequestDto

interface TeamSolutionApi {
    /**
     * POST api/team-solution/{solutionId}/review
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param solutionId 
     * @param updateTeamSolutionRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @POST("api/team-solution/{solutionId}/review")
    suspend fun apiTeamSolutionSolutionIdReviewPost(@Path("solutionId") solutionId: java.util.UUID, @Body updateTeamSolutionRequestDto: UpdateTeamSolutionRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * DELETE api/team-task/{taskId}/solution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [IdRequestDtoApiResponse]
     */
    @DELETE("api/team-task/{taskId}/solution")
    suspend fun apiTeamTaskTaskIdSolutionDelete(@Path("taskId") taskId: java.util.UUID): Response<IdRequestDtoApiResponse>

    /**
     * GET api/team-task/{taskId}/solution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [StudentTeamSolutionDetailsDtoApiResponse]
     */
    @GET("api/team-task/{taskId}/solution")
    suspend fun apiTeamTaskTaskIdSolutionGet(@Path("taskId") taskId: java.util.UUID): Response<StudentTeamSolutionDetailsDtoApiResponse>

    /**
     * PUT api/team-task/{taskId}/solution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @param submitTeamSolutionRequestDto  (optional)
     * @return [IdRequestDtoApiResponse]
     */
    @PUT("api/team-task/{taskId}/solution")
    suspend fun apiTeamTaskTaskIdSolutionPut(@Path("taskId") taskId: java.util.UUID, @Body submitTeamSolutionRequestDto: SubmitTeamSolutionRequestDto? = null): Response<IdRequestDtoApiResponse>

    /**
     * GET api/team-task/{taskId}/solutions
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @param skip  (optional, default to 0)
     * @param take  (optional, default to 20)
     * @param status  (optional)
     * @param teamId  (optional)
     * @return [TeamSolutionListDtoApiResponse]
     */
    @GET("api/team-task/{taskId}/solutions")
    suspend fun apiTeamTaskTaskIdSolutionsGet(@Path("taskId") taskId: java.util.UUID, @Query("skip") skip: kotlin.Int? = 0, @Query("take") take: kotlin.Int? = 20, @Query("status") status: SolutionStatus? = null, @Query("teamId") teamId: java.util.UUID? = null): Response<TeamSolutionListDtoApiResponse>

}
