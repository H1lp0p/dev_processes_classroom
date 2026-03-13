package com.stuf.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreTest {

    @Test
    fun `score accepts non-negative values`() {
        val score = Score(5)
        assertEquals(5, score.value)
    }

    @Test
    fun `score accepts zero`() {
        val score = Score(0)
        assertEquals(0, score.value)
    }

    @Test
    fun `score accepts large positive values`() {
        val score = Score(10_000)
        assertEquals(10_000, score.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `score rejects negative values`() {
        Score(-1)
    }
}

