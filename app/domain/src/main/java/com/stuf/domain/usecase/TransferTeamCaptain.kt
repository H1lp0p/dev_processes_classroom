package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId

interface TransferTeamCaptain {
    suspend operator fun invoke(teamId: TeamId, toUserId: UserId): DomainResult<Unit>
}
