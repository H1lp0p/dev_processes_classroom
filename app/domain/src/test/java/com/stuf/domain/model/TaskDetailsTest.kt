package com.stuf.domain.model

import org.junit.Test

class TaskDetailsTest {

    @Test
    fun `task details accepts positive maxScore`() {
        TaskDetails(
            deadline = null,
            isMandatory = true,
            maxScore = 1,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `task details rejects non-positive maxScore`() {
        TaskDetails(
            deadline = null,
            isMandatory = false,
            maxScore = 0,
        )
    }
}

