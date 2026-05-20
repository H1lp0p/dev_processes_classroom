package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TeamId

interface LeaveTeam {
    suspend operator fun invoke(teamId: TeamId): DomainResult<Unit>
}
