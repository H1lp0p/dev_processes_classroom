package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId

interface GetGradeDistribution {
    suspend operator fun invoke(teamId: TeamId, assignmentId: PostId): DomainResult<GradeDistribution>
}
