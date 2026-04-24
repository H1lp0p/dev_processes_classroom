package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeVote
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId

interface VoteOnGradeDistribution {
    suspend operator fun invoke(
        teamId: TeamId,
        assignmentId: PostId,
        vote: GradeVote,
    ): DomainResult<Unit>
}
