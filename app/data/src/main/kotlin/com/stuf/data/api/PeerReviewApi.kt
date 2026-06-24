package com.stuf.data.api

import com.stuf.data.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import com.stuf.data.model.PeerReviewProgressDtoApiResponse
import com.stuf.data.model.PeerReviewTargetDtoApiResponse
import com.stuf.data.model.PeerReviewTeamTargetListDtoApiResponse
import com.stuf.data.model.SubmitPeerReviewDto

interface PeerReviewApi {
    /**
     * POST api/peer-review/{reviewId}/submit
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param reviewId 
     * @param submitPeerReviewDto  (optional)
     * @return [PeerReviewProgressDtoApiResponse]
     */
    @POST("api/peer-review/{reviewId}/submit")
    suspend fun apiPeerReviewReviewIdSubmitPost(@Path("reviewId") reviewId: java.util.UUID, @Body submitPeerReviewDto: SubmitPeerReviewDto? = null): Response<PeerReviewProgressDtoApiResponse>

    /**
     * POST api/task/{taskId}/peer-review/finish
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [PeerReviewProgressDtoApiResponse]
     */
    @POST("api/task/{taskId}/peer-review/finish")
    suspend fun apiTaskTaskIdPeerReviewFinishPost(@Path("taskId") taskId: java.util.UUID): Response<PeerReviewProgressDtoApiResponse>

    /**
     * GET api/task/{taskId}/peer-review/next
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [PeerReviewTargetDtoApiResponse]
     */
    @GET("api/task/{taskId}/peer-review/next")
    suspend fun apiTaskTaskIdPeerReviewNextGet(@Path("taskId") taskId: java.util.UUID): Response<PeerReviewTargetDtoApiResponse>

    /**
     * GET api/task/{taskId}/peer-review/progress
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [PeerReviewProgressDtoApiResponse]
     */
    @GET("api/task/{taskId}/peer-review/progress")
    suspend fun apiTaskTaskIdPeerReviewProgressGet(@Path("taskId") taskId: java.util.UUID): Response<PeerReviewProgressDtoApiResponse>

    /**
     * POST api/team-solution/{teamSolutionId}/peer-review
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param teamSolutionId 
     * @param submitPeerReviewDto  (optional)
     * @return [PeerReviewProgressDtoApiResponse]
     */
    @POST("api/team-solution/{teamSolutionId}/peer-review")
    suspend fun apiTeamSolutionTeamSolutionIdPeerReviewPost(@Path("teamSolutionId") teamSolutionId: java.util.UUID, @Body submitPeerReviewDto: SubmitPeerReviewDto? = null): Response<PeerReviewProgressDtoApiResponse>

    /**
     * GET api/team-task/{taskId}/peer-review/available
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [PeerReviewTeamTargetListDtoApiResponse]
     */
    @GET("api/team-task/{taskId}/peer-review/available")
    suspend fun apiTeamTaskTaskIdPeerReviewAvailableGet(@Path("taskId") taskId: java.util.UUID): Response<PeerReviewTeamTargetListDtoApiResponse>

    /**
     * GET api/team-task/{taskId}/peer-review/progress
     * 
     * 
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 403: Forbidden
     *
     * @param taskId 
     * @return [PeerReviewProgressDtoApiResponse]
     */
    @GET("api/team-task/{taskId}/peer-review/progress")
    suspend fun apiTeamTaskTaskIdPeerReviewProgressGet(@Path("taskId") taskId: java.util.UUID): Response<PeerReviewProgressDtoApiResponse>

}
