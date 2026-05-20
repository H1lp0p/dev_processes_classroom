package com.stuf.domain.model

data class GradeTable(
    val tasks: List<TaskId>,
    val rows: List<GradeRow>,
)
