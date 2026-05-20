package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId

interface GradeDistributionRepository {
    suspend fun getGradeDistribution(teamId: TeamId, assignmentId: PostId): DomainResult<GradeDistribution>

    suspend fun updateGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution>

    suspend fun voteOnGradeDistribution(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
    ): DomainResult<Unit>
}
