package com.stuf.domain.model

data class GradeCell(
    val status: GradeStatus,
    val score: Score?,
)

enum class GradeStatus {
    NOT_SUBMITTED,
    PENDING_REVIEW,
    RETURNED,
    GRADED,
}

data class GradeRow(
    val student: CourseMember,
    val cells: Map<TaskId, GradeCell>,
    val averageScore: Double,
)

data class GradeTable(
    val tasks: List<TaskId>,
    val rows: List<GradeRow>,
)

