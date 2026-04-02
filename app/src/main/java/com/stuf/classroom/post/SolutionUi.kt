package com.stuf.classroom.post

import com.stuf.domain.model.SolutionId

data class SolutionUi(
    val id: SolutionId,
    val studentName: String,
    val status: String,
)
