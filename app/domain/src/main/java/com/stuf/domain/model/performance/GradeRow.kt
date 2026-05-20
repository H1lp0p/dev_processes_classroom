package com.stuf.domain.model

data class GradeRow(
    val student: CourseMember,
    val cells: Map<TaskId, GradeCell>,
    val averageScore: Double,
)
