package com.stuf.domain.model

data class Team(
    val id: TeamId,
    val name: String,
    val members: List<TeamMember>,
)

data class TeamMember(
    val userId: UserId,
    val credentials: String,
    val role: TeamMemberRole,
)

enum class TeamMemberRole {
    MEMBER,
    LEADER,
}
