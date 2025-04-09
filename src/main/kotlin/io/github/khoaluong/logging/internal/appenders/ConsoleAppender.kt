package io.github.khoaluong.logging.internal.appenders

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter

/**
 * An appender that writes formatted log events to the standard console output (`System.out` or `System.err`).
 *
 * @param formatter The formatter to use for converting LogEvents to strings. Defaults to SimpleFormatter.
 * @param target Specifies the output stream: "stdout" (default) or "stderr".
 */
class ConsoleAppender(
    override val formatter: Formatter = SimpleFormatter(),
    private val target: String = "stdout"
) : Appender {

    private val outputStream = if (target.equals("stderr", ignoreCase = true)) System.err else System.out

    /**
     * Formats the event and prints it to the configured console stream.
     * `println` is generally thread-safe.
     */
    override fun append(event: LogEvent) {
        try {
            val formattedMessage = formatter.format(event)
            // Use synchronized block if formatter is potentially stateful or complex operations happen
            // synchronized(this) {
            outputStream.println(formattedMessage)
            // }
        } catch (e: Exception) {
            System.err.println("ERROR: ConsoleAppender failed to format/print message: ${e.message}")
            // Avoid recursive logging if System.err itself is the target
        }
    }

    // No resources to stop/release for console appender
    override fun stop() {}

    override fun toString(): String {
        return "ConsoleAppender(target='$target', formatter=${formatter::class.simpleName})"
    }
}