package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TeamId

interface CheckTeamCaptain {
    suspend operator fun invoke(teamId: TeamId): DomainResult<Boolean>
}
