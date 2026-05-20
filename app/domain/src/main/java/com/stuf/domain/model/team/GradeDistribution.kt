package com.stuf.domain.model

data class GradeDistribution(
    val teamId: TeamId,
    val assignmentId: PostId,
    /** Оценка всей команды (Rraw), выставленная преподавателем. */
    val teamRawScore: Double,
    val entries: List<GradeDistributionEntry>,
    val sumDistributed: Double,
    val distributionChanged: Boolean,
    /** Голос текущего пользователя по предложенному распределению (если API отдаёт). */
    val currentUserVote: GradeVote? = null,
)

data class GradeDistributionEntry(
    val userId: UserId,
    val points: Double,
)

enum class GradeVote {
    FOR,
    AGAINST,
}
