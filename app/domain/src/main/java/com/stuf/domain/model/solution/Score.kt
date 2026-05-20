package com.stuf.domain.model

@JvmInline
value class Score(val value: Int) {
    init {
        require(value >= 0) { "Score must be non-negative" }
    }
}
