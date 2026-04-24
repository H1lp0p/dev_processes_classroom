package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.GradeDistribution
import com.stuf.domain.model.GradeDistributionEntry
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TeamId

interface SaveGradeDistribution {
    suspend operator fun invoke(
        teamId: TeamId,
        assignmentId: PostId,
        entries: List<GradeDistributionEntry>,
    ): DomainResult<GradeDistribution>
}
