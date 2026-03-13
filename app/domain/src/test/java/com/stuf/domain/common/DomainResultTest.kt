package com.stuf.domain.common

import org.junit.Assert.assertTrue
import org.junit.Test

class DomainResultTest {

    @Test
    fun `success wraps value`() {
        val result: DomainResult<Int> = DomainResult.Success(42)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `failure wraps error`() {
        val error = DomainError.Validation("oops")
        val result: DomainResult<Nothing> = DomainResult.Failure(error)
        assertTrue(result is DomainResult.Failure)
    }
}

