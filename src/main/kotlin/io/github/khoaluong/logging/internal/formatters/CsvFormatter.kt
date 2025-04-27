package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.time.format.DateTimeFormatter

/**
 * Formats log events as Comma Separated Value (CSV) lines.
 * Columns: Timestamp, Level, ThreadName, LoggerName, Message, ContextDataJson, Throwable
 */
class CsvFormatter(
    private val contextFormatter: (Map<String, String>) -> String = { map ->
        if (map.isEmpty()) ""
        else try {
            map.entries.joinToString(prefix = "{", postfix = "}", separator = ",") {
                "\"${escapeCsv(it.key)}\":\"${escapeCsv(it.value)}\""
            }
        } catch (e: NoClassDefFoundError) {
            map.toString()
        }
    }
) : Formatter {

    companion object {
        fun escapeCsv(field: String?): String {
            if (field == null) return ""
            val text = field.replace("\"", "\"\"") // Escape quotes
            return if (text.contains(',') || text.contains('"') || text.contains('\n') || text.contains('\r')) {
                "\"$text\""
            } else {
                text
            }
        }
    }

    private val dateFormatter = DateTimeFormatter.ISO_INSTANT

    override fun format(event: LogEvent): String {
        val throwableString = event.throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        } ?: ""

        val contextString = contextFormatter(event.contextData)

        return listOf(
            dateFormatter.format(event.timestamp),
            event.level.name,
            event.threadName,
            event.loggerName,
            event.message,
            contextString,
            throwableString
        ).joinToString(",") { escapeCsv(it) }
    }
}