package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.BooleanApiResponse
import com.stuf.data.model.ObjectApiResponse
import com.stuf.data.model.RenameTeamRequestDto
import com.stuf.data.model.TeamDtoApiResponse
import com.stuf.data.model.TeamDtoListApiResponse

interface TeamApi {
    /**
     * GET api/teacher/team-task/{assignmentId}/teams
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param assignmentId 
     * @return [TeamDtoListApiResponse]
     */
    @GET("api/teacher/team-task/{assignmentId}/teams")
    suspend fun apiTeacherTeamTaskAssignmentIdTeamsGet(@Path("assignmentId") assignmentId: java.util.UUID): Response<TeamDtoListApiResponse>

    /**
     * POST api/teacher/teams/{teamId}/add-student/{studentId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param studentId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teacher/teams/{teamId}/add-student/{studentId}")
    suspend fun apiTeacherTeamsTeamIdAddStudentStudentIdPost(@Path("teamId") teamId: java.util.UUID, @Path("studentId") studentId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * POST api/teacher/teams/{teamId}/fixed-captain
     * 
     * 
     * Responses:
     *  - 200: OK
     *
     * @param teamId 
     * @param body  (optional)
     * @return [Unit]
     */
    @POST("api/teacher/teams/{teamId}/fixed-captain")
    suspend fun apiTeacherTeamsTeamIdFixedCaptainPost(@Path("teamId") teamId: java.util.UUID, @Body body: java.util.UUID? = null): Response<Unit>

    /**
     * DELETE api/teacher/teams/{teamId}/remove-student/{studentId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param studentId 
     * @return [ObjectApiResponse]
     */
    @DELETE("api/teacher/teams/{teamId}/remove-student/{studentId}")
    suspend fun apiTeacherTeamsTeamIdRemoveStudentStudentIdDelete(@Path("teamId") teamId: java.util.UUID, @Path("studentId") studentId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * PUT api/teacher/teams/{teamId}/rename
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param renameTeamRequestDto  (optional)
     * @return [ObjectApiResponse]
     */
    @PUT("api/teacher/teams/{teamId}/rename")
    suspend fun apiTeacherTeamsTeamIdRenamePut(@Path("teamId") teamId: java.util.UUID, @Body renameTeamRequestDto: RenameTeamRequestDto? = null): Response<ObjectApiResponse>

    /**
     * GET api/team-task/{assignmentId}/my-team
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 404: Not Found
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param assignmentId 
     * @return [TeamDtoApiResponse]
     */
    @GET("api/team-task/{assignmentId}/my-team")
    suspend fun apiTeamTaskAssignmentIdMyTeamGet(@Path("assignmentId") assignmentId: java.util.UUID): Response<TeamDtoApiResponse>

    /**
     * GET api/team-task/{assignmentId}/teams
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param assignmentId 
     * @return [TeamDtoListApiResponse]
     */
    @GET("api/team-task/{assignmentId}/teams")
    suspend fun apiTeamTaskAssignmentIdTeamsGet(@Path("assignmentId") assignmentId: java.util.UUID): Response<TeamDtoListApiResponse>

    /**
     * GET api/teams/{teamId}/is-captain
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @return [BooleanApiResponse]
     */
    @GET("api/teams/{teamId}/is-captain")
    suspend fun apiTeamsTeamIdIsCaptainGet(@Path("teamId") teamId: java.util.UUID): Response<BooleanApiResponse>

    /**
     * POST api/teams/{teamId}/join
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teams/{teamId}/join")
    suspend fun apiTeamsTeamIdJoinPost(@Path("teamId") teamId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * POST api/teams/{teamId}/leave
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teams/{teamId}/leave")
    suspend fun apiTeamsTeamIdLeavePost(@Path("teamId") teamId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * POST api/teams/{teamId}/start-voting
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teams/{teamId}/start-voting")
    suspend fun apiTeamsTeamIdStartVotingPost(@Path("teamId") teamId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * POST api/teams/{teamId}/transfer-captain/{toUserId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param toUserId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teams/{teamId}/transfer-captain/{toUserId}")
    suspend fun apiTeamsTeamIdTransferCaptainToUserIdPost(@Path("teamId") teamId: java.util.UUID, @Path("toUserId") toUserId: java.util.UUID): Response<ObjectApiResponse>

    /**
     * POST api/teams/{teamId}/vote/{candidateId}
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamId 
     * @param candidateId 
     * @return [ObjectApiResponse]
     */
    @POST("api/teams/{teamId}/vote/{candidateId}")
    suspend fun apiTeamsTeamIdVoteCandidateIdPost(@Path("teamId") teamId: java.util.UUID, @Path("candidateId") candidateId: java.util.UUID): Response<ObjectApiResponse>

}
