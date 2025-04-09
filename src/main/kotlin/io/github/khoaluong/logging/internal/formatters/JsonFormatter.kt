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

/**
 * Formats log events as JSON strings using kotlinx.serialization.
 * Requires the `kotlinx-serialization-json` dependency and the serialization plugin.
 *
 * @param json The kotlinx.serialization Json instance to use (allows customization).
 * @param includeStackTrace Whether to include the stack trace for throwables (can be verbose).
 */
class JsonFormatter(
    private val json: Json = Json {
        encodeDefaults = true // Include properties with default values
        prettyPrint = false   // Use compact JSON output
        serializersModule = SerializersModule {
            contextual(InstantSerializer) // Register custom serializer for Instant
            // Register custom serializer for Throwable - it MUST be registered here
            contextual(ThrowableSerializer)
        }
    },
    private val includeStackTrace: Boolean = true
) : Formatter {

    // Custom serializer for Instant
    // (No @Serializer needed if manually implementing KSerializer and registering contextually)
    object InstantSerializer : KSerializer<Instant> {
        // Use a standard name for the descriptor
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeString(value.toString()) // ISO-8601 format
        }
        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }
    }

    // Custom serializer for Throwable
    // REMOVED: @Serializer(forClass = Throwable::class) - Cannot generate for external class
    object ThrowableSerializer : KSerializer<Throwable> {
        // ADDED: Manually implement the descriptor
        // We are serializing it as a simple String (the stack trace)
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.lang.Throwable", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Throwable) {
            val sw = StringWriter()
            value.printStackTrace(PrintWriter(sw))
            // Potentially limit stack trace length here if needed
            encoder.encodeString(sw.toString())
        }

        override fun deserialize(decoder: Decoder): Throwable {
            // Deserialization of a Throwable from a stack trace string is generally not practical
            // or safe. Return a placeholder or throw an exception.
            val stackTrace = decoder.decodeString()
            // Option 1: Throw exception (usually preferred)
            // throw SerializationException("Cannot deserialize Throwable from stack trace string")
            // Option 2: Return a generic exception with the trace as the message
            return RuntimeException("Deserialized Throwable: $stackTrace")
            // Option 3: Return null if the field is nullable (adjust calling code)
            // return null
        }
    }


    @Serializable // Temporary holder class to control serialization
    private data class SerializableLogEvent(
        @Contextual // Use the contextually registered InstantSerializer
        val timestamp: Instant,
        val level: String, // Serialize level as string
        val loggerName: String,
        val threadName: String,
        val message: String,
        // Use the contextually registered ThrowableSerializer
        // Make it nullable if you might not include it
        @Contextual
        val throwable: Throwable? = null,
        val contextData: Map<String, String> = emptyMap()
    )

    override fun format(event: LogEvent): String {
        try {
            // Map LogEvent to a representation suitable for serialization
            val serializableEvent = SerializableLogEvent(
                timestamp = event.timestamp,
                level = event.level.name, // Use level name string
                loggerName = event.loggerName,
                threadName = event.threadName,
                message = event.message,
                // Conditionally include the throwable based on the flag
                throwable = if (includeStackTrace) event.throwable else null,
                contextData = event.contextData
            )
            // Pass the correct serializer explicitly (serializer() generated for @Serializable class)
            return json.encodeToString(SerializableLogEvent.serializer(), serializableEvent)
            // Alternative using reified types (if json instance is available elsewhere):
            // return json.encodeToString(serializableEvent) // Relies on contextual serializers
        } catch (e: Exception) {
            // Fallback or error logging if serialization fails
            // Consider logging the exception 'e' itself using a fallback logger
            // Avoid complex string interpolation in the fallback itself
            return """{"error":"Failed to serialize log event","details":"${e::class.simpleName} - ${e.message?.replace("\"", "\\\"")}","event_message":"${event.message.replace("\"", "\\\"")}"}"""
        }
    }
}