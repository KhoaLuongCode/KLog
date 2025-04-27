package io.github.khoaluong.logging.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

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