package com.stuf.data.infrastructure

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Moshi's default enum adapter matches [Enum.name] exactly.
 * OpenAPI-generated enums expose companion [decode] with case-insensitive matching
 * (needed for ASP.NET camelCase values like `teacherReview` vs `TeacherReview`).
 */
class OpenApiEnumJsonAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = Types.getRawType(type)
        if (!rawType.isEnum) return null
        val decode = resolveDecodeMethod(rawType) ?: return null
        return Adapter(rawType, decode).nullSafe()
    }

    private fun resolveDecodeMethod(rawType: Class<*>): Method? =
        try {
            val companion = rawType.getDeclaredField("Companion").get(null)
            companion.javaClass.getDeclaredMethod("decode", Any::class.java)
        } catch (_: ReflectiveOperationException) {
            null
        }

    private class Adapter(
        private val rawType: Class<*>,
        private val decode: Method,
    ) : JsonAdapter<Any>() {

        private val companion: Any =
            rawType.getDeclaredField("Companion").get(null)

        override fun fromJson(reader: JsonReader): Any? {
            if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull<Unit>()
                return null
            }
            val value = reader.nextString()
            val decoded = decode.invoke(companion, value)
                ?: throw JsonDataException(
                    "Unknown enum value '$value' for ${rawType.simpleName}",
                )
            return decoded
        }

        override fun toJson(writer: JsonWriter, value: Any?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            val enumValue = value as Enum<*>
            val serialized =
                try {
                    val getValue = value.javaClass.getMethod("getValue")
                    getValue.invoke(value) as String
                } catch (_: ReflectiveOperationException) {
                    enumValue.toString()
                }
            writer.value(serialized)
        }
    }
}
