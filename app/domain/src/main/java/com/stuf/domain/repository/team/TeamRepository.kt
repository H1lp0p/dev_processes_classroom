package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId
import com.stuf.domain.model.Team
import com.stuf.domain.model.TeamId
import com.stuf.domain.model.UserId

interface TeamRepository {
    suspend fun getTeamsForAssignment(assignmentId: PostId): DomainResult<List<Team>>

    suspend fun getMyTeam(assignmentId: PostId): DomainResult<Team?>

    suspend fun joinTeam(teamId: TeamId): DomainResult<Unit>

    suspend fun leaveTeam(teamId: TeamId): DomainResult<Unit>

    suspend fun transferCaptain(teamId: TeamId, toUserId: UserId): DomainResult<Unit>

    suspend fun isCaptain(teamId: TeamId): DomainResult<Boolean>
}
