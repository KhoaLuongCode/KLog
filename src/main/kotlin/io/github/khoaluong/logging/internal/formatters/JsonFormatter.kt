package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

class JsonFormatter(
    private val json: Json = Json {
        encodeDefaults = true
        prettyPrint = false
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
            contextual(ThrowableSerializer)
        }
    },
    private val includeStackTrace: Boolean = true
) : Formatter {

    object InstantSerializer : KSerializer<Instant> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeString(value.toString())
        }
        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }
    }

    object ThrowableSerializer : KSerializer<Throwable> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.lang.Throwable", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Throwable) {
            val sw = StringWriter()
            value.printStackTrace(PrintWriter(sw))
            encoder.encodeString(sw.toString())
        }

        override fun deserialize(decoder: Decoder): Throwable {
            val stackTrace = decoder.decodeString()
            return RuntimeException("Deserialized Throwable: $stackTrace")
        }
    }


    @Serializable
    private data class SerializableLogEvent(
        @Contextual
        val timestamp: Instant,
        val level: String,
        val loggerName: String,
        val threadName: String,
        val message: String,

        @Contextual
        val throwable: Throwable? = null,
        val contextData: Map<String, String> = emptyMap()
    )

    override fun format(event: LogEvent): String {
        return try {
            val serializableEvent = SerializableLogEvent(
                timestamp = event.timestamp,
                level = event.level.name,
                loggerName = event.loggerName,
                threadName = event.threadName,
                message = event.message,
                throwable = if (includeStackTrace) event.throwable else null,
                contextData = event.contextData
            )
            json.encodeToString(SerializableLogEvent.serializer(), serializableEvent)
        } catch (e: Exception) {
            """{"error":"Failed to serialize log event","details":"${e::class.simpleName} - ${e.message?.replace("\"", "\\\"")}","event_message":"${event.message.replace("\"", "\\\"")}"}"""
        }
    }
}