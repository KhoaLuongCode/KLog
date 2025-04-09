package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.time.format.DateTimeFormatter

/**
 * Formats log events as Comma Separated Value (CSV) lines.
 * Note: Does basic CSV escaping (quotes for fields containing commas or quotes).
 * Header line is not included by this formatter itself.
 *
 * Columns: Timestamp, Level, ThreadName, LoggerName, Message, ContextDataJson, Throwable
 */
class CsvFormatter(
    // Example: Use kotlinx.serialization to format context map as JSON string within CSV
    private val contextFormatter: (Map<String, String>) -> String = { map ->
        if (map.isEmpty()) ""
        else try {
            // Minimal JSON-like escaping, or use kotlinx.serialization.json if available
            map.entries.joinToString(prefix = "{", postfix = "}", separator = ",") {
                "\"${escapeCsv(it.key)}\":\"${escapeCsv(it.value)}\""
            }
        } catch (e: NoClassDefFoundError) {
            // Fallback if kotlinx.serialization is not present
            map.toString()
        }
    }
) : Formatter {

    companion object {
        // Simple CSV escaping: quote fields containing comma, quote, or newline
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

    // Using ISO standard format for easier parsing
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
        ).joinToString(",") { escapeCsv(it) } // Apply escaping to all fields
    }
}