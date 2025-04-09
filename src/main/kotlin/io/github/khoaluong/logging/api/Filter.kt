package io.github.khoaluong.logging.api

/**
 * Interface for filtering log events before they are passed to appenders.
 */
fun interface Filter {
    /**
     * Decides whether a log event should be processed further (passed to appenders).
     *
     * @param event The log event to check.
     * @return `true` if the event should be logged, `false` if it should be discarded.
     */
    fun filter(event: LogEvent): Boolean
}