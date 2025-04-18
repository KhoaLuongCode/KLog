package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A simple formatter producing human-readable output.
 * Format: [YYYY-MM-DD HH:MM:SS.sss] [LEVEL] [ThreadName] LoggerName - Message {ContextData} {Exception}
 */
class SimpleFormatter(
    zoneId: ZoneId = ZoneId.systemDefault()
) : Formatter {

    // Consider making the pattern configurable
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(zoneId)

    override fun format(event: LogEvent): String {
        val sb = StringBuilder()
        sb.append(
            "[${dateFormatter.format(event.timestamp)}] " + "[${event.level.name.padEnd(5)}] " + "[${event.threadName}|${event.coroutineContext}] - " + event.message
        )


        if (event.contextData.isNotEmpty()) {
            sb.append(" ")
            sb.append(event.contextData) // Simple map toString representation
        }

        event.throwable?.let {
            sb.append(System.lineSeparator()) // New line for stack trace
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sb.append(sw.toString())
        }

        sb.append(System.lineSeparator()) // New line at the end

        return sb.toString()
    }
}