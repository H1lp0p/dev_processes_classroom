package com.stuf.data.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class OffsetDateTimeAdapterTest {

    @Test
    fun `parse accepts offset date-time`() {
        assertEquals(
            OffsetDateTime.parse("2024-01-01T10:00:00Z"),
            OffsetDateTimeAdapter.parse("2024-01-01T10:00:00Z"),
        )
    }

    @Test
    fun `parse accepts local date-time from backend feed`() {
        assertEquals(
            OffsetDateTime.of(2026, 5, 21, 14, 2, 36, 58_745_000, ZoneOffset.UTC),
            OffsetDateTimeAdapter.parse("2026-05-21T14:02:36.058745"),
        )
    }
}
