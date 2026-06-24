package com.stuf.data.infrastructure

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses [OffsetDateTime] from API strings with or without timezone offset.
 * Backend feed uses local date-time, e.g. `2026-05-21T14:02:36.058745`.
 */
class OffsetDateTimeAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) != OffsetDateTime::class.java) {
            return null
        }
        return Adapter().nullSafe()
    }

    private class Adapter : JsonAdapter<OffsetDateTime>() {
        override fun fromJson(reader: JsonReader): OffsetDateTime {
            return parse(reader.nextString())
        }

        override fun toJson(writer: JsonWriter, value: OffsetDateTime?) {
            writer.value(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
        }
    }

    companion object {
        fun parse(value: String): OffsetDateTime {
            try {
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } catch (_: DateTimeParseException) {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atOffset(ZoneOffset.UTC)
            }
        }
    }
}
