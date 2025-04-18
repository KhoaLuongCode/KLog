package io.github.khoaluong.logging.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * Represents a single logging event containing all relevant information.
 * This object is passed through filters and to appenders.
 *
 * @property timestamp The exact time the event occurred.
 * @property level The severity level of the event.
 * @property loggerName The name of the logger that generated the event.
 * @property threadName The name of the thread where the event occurred.
 * @property message The log message content (often formatted later).
 * @property throwable Optional exception associated with the event.
 * @property contextData Optional map containing contextual data (e.g., from coroutine context).
 */

@Serializable
data class LogEvent(
    @Serializable(with = InstantSerializer::class) val timestamp: Instant = Instant.now(),
    val level: LogLevel,
    val loggerName: String,
    val threadName: String = Thread.currentThread().name,
    val message: String,
    val coroutineContext: String = "",
    @Transient val throwable: Throwable? = null,
    val contextData: Map<String, String> = emptyMap()
)

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}