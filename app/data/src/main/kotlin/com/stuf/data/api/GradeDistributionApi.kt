package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.GradeDistributionResponseDtoApiResponse
import com.stuf.data.model.GradeDistributionUpdateRequestDto
import com.stuf.data.model.GradeDistributionVoteRequestDto
import com.stuf.data.model.ObjectApiResponse

interface GradeDistributionApi {
    /**
     * GET api/teams/{teamId}/assignments/{assignmentId}/grade-distribution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param assignmentId 
     * @return [GradeDistributionResponseDtoApiResponse]
     */
    @GET("api/teams/{teamId}/assignments/{assignmentId}/grade-distribution")
    suspend fun apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionGet(@Path("teamId") teamId: java.util.UUID, @Path("assignmentId") assignmentId: java.util.UUID): Response<GradeDistributionResponseDtoApiResponse>

    /**
     * PUT api/teams/{teamId}/assignments/{assignmentId}/grade-distribution
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 400: Bad Request
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param assignmentId 
     * @param gradeDistributionUpdateRequestDto  (optional)
     * @return [GradeDistributionResponseDtoApiResponse]
     */
    @PUT("api/teams/{teamId}/assignments/{assignmentId}/grade-distribution")
    suspend fun apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionPut(@Path("teamId") teamId: java.util.UUID, @Path("assignmentId") assignmentId: java.util.UUID, @Body gradeDistributionUpdateRequestDto: GradeDistributionUpdateRequestDto? = null): Response<GradeDistributionResponseDtoApiResponse>

    /**
     * POST api/teams/{teamId}/assignments/{assignmentId}/grade-distribution/vote
     * 
     * 
     * Responses:
     *  - 204: No Content
     *  - 400: Bad Request
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param assignmentId 
     * @param gradeDistributionVoteRequestDto  (optional)
     * @return [Unit]
     */
    @POST("api/teams/{teamId}/assignments/{assignmentId}/grade-distribution/vote")
    suspend fun apiTeamsTeamIdAssignmentsAssignmentIdGradeDistributionVotePost(@Path("teamId") teamId: java.util.UUID, @Path("assignmentId") assignmentId: java.util.UUID, @Body gradeDistributionVoteRequestDto: GradeDistributionVoteRequestDto? = null): Response<Unit>

}
