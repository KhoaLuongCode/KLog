package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.api.LogLevel
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Format: [YYYY-MM-DD HH:MM:SS.sss] [LEVEL] [ThreadName] LoggerName - Message {ContextData} {Exception}
 */
class SimpleFormatter(
    zoneId: ZoneId = ZoneId.systemDefault()
) : Formatter {

    companion object {
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_YELLOW = "\u001B[33m"
        const val ANSI_BLUE = "\u001B[34m"
        const val ANSI_PURPLE = "\u001B[35m"

        const val ANSI_RED_BOLD = "\u001B[1;31m"

        private fun getColorForLevel(level: LogLevel): String {
            return when (level) {
                LogLevel.TRACE -> ANSI_PURPLE
                LogLevel.DEBUG -> ANSI_BLUE
                LogLevel.INFO  -> ANSI_GREEN
                LogLevel.WARN  -> ANSI_YELLOW
                LogLevel.ERROR -> ANSI_RED_BOLD
            }
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(zoneId)


    override fun format(event: LogEvent): String {
        val sb = StringBuilder()
        val color = getColorForLevel(event.level)

        sb.append(color)

        val coroutineContextStr = if (event.coroutineContext.isNotEmpty()) "|${event.coroutineContext}" else ""

        sb.append(
            "[${dateFormatter.format(event.timestamp)}] " + "[${event.level.name.padEnd(5)}] " + "[${event.threadName}|${coroutineContextStr}] - " + event.message
        )


        if (event.contextData.isNotEmpty()) {
            sb.append(" ")
            sb.append(event.contextData)
        }

        event.throwable?.let {
            sb.append(System.lineSeparator())
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sb.append(sw.toString())
        }

        sb.append(ANSI_RESET)
        sb.append(System.lineSeparator())

        return sb.toString()
    }
}