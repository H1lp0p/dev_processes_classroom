package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Team

interface GetMyTeamForTeamTask {
    suspend operator fun invoke(assignmentId: PostId): DomainResult<Team?>
}
