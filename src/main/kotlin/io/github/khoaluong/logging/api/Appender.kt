package io.github.khoaluong.logging.api

/**
 * Interface for log output destinations.
 * Appenders are responsible for writing formatted log events to specific targets (console, file, network, etc.).
 */
interface Appender {
    /**
     * The formatter used by this appender to convert LogEvent objects into strings.
     */
    val formatter: Formatter

    /**
     * Processes and outputs a log event.
     * Implementations should handle the actual writing to the destination (e.g., console, file).
     * Implementations MUST be thread-safe if they manage shared resources (like file handles).
     *
     * @param event The log event to append.
     */
    suspend fun append(event: LogEvent)

    /**
     * Optional: Called when the logging system is shutting down to release resources (e.g., close file handles).
     * Default implementation does nothing.
     */
    fun start()
    suspend fun stop()
}