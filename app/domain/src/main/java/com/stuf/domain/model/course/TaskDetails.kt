package com.stuf.domain.model

import java.time.OffsetDateTime

data class TaskDetails(
    val deadline: OffsetDateTime?,
    val isMandatory: Boolean,
    val maxScore: Int = 5,
) {
    init {
        require(maxScore > 0) { "maxScore must be positive" }
    }
}
