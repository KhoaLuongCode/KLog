package io.github.khoaluong.logging.internal.appenders

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import io.github.khoaluong.logging.io.ChannelWriter
import io.github.khoaluong.logging.io.KLogWriter
import kotlinx.coroutines.channels.Channel

class ConsoleAppender private constructor(
    override val formatter: Formatter,
    private val target: ConsoleTarget,
    private val id: String,
    private val channel: Channel<String>
) : Appender {
    enum class ConsoleTarget {
        STDOUT, STDERR
    }
    companion object {
        private val appenders = mutableMapOf<ConsoleTarget, Appender>()

        fun createConsoleAppender(formatter: Formatter = SimpleFormatter(), target: ConsoleTarget): Appender {
            if (appenders.containsKey(target)) {
                return appenders[target]!!
            }
            val id = java.util.UUID.randomUUID().toString()
            val outputStream = if (target == ConsoleTarget.STDOUT) System.out else System.err
            val channel = KLogWriter.createWriter(id, 1000, outputStream)
            val appender = ConsoleAppender(formatter, target, id, channel)

            appenders[target] = appender
            return appender
        }
    }

    override suspend fun append(event: LogEvent) {
        try {
            val formattedMessage = formatter.format(event)
            channel.send(formattedMessage) // Send formatted message to the channel
        } catch (e: Exception) {
            //System.err.println("ERROR: ConsoleAppender failed to format/print message: ${e.message}")
            // Avoid recursive logging if System.err itself is the target
            //TODO handle this error more gracefully
            e.printStackTrace()
        }
    }

    override fun start() {
        KLogWriter.startWriter(id)
    }

    override suspend fun stop() {
        KLogWriter.stopWriter(id)
    }

    override fun toString(): String {
        return "ConsoleAppender(target='$target', formatter=${formatter::class.simpleName})"
    }
}