package com.stuf.domain.usecase

import java.time.OffsetDateTime

data class PerformanceFilters(
    val query: String? = null,
    val from: OffsetDateTime? = null,
    val to: OffsetDateTime? = null,
    val onlyMandatory: Boolean = false,
)
