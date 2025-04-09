package io.github.khoaluong.logging.api

/**
 * Interface for formatting LogEvent objects into strings.
 */
fun interface Formatter {
    /**
     * Formats a log event into a string representation.
     *
     * @param event The log event to format.
     * @return The formatted string representation of the log event.
     */
    fun format(event: LogEvent): String
}