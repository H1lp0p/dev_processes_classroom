package com.stuf.grading.domain.model

import java.util.UUID

@JvmInline
value class CriterionId(val value: String) {
    companion object {
        fun random(): CriterionId = CriterionId(UUID.randomUUID().toString())
    }
}
