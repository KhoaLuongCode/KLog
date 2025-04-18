package io.github.khoaluong.logging.api

import io.github.khoaluong.logging.internal.DefaultLogger
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.appenders.ConsoleAppender

/**
 * Factory object for obtaining Logger instances.
 * This is the main entry point for users to get loggers.
 * It also provides basic configuration capabilities.
 */
object LoggerFactory {

    /**
     * Retrieves a logger instance with the specified name.
     * If a logger with that name already exists, it's returned; otherwise, a new one is created.
     *
     * @param name The name of the logger (often the fully qualified class name).
     * @return The Logger instance.
     */
    fun getLogger(name: String): Logger {
        return LogDispatcher.getLogger(name)
    }

    /**
     * Retrieves a logger instance named after the calling class.
     * Note: This involves stack trace inspection and might have a performance overhead.
     * Prefer `getLogger(Class<*>)` or `getLogger(KClass<*>)` via extensions if possible.
     *
     * @return The Logger instance named after the caller class.
     */
    fun getLogger(): Logger {
        // Caution: Getting stack trace element is relatively expensive.
        val callerClassName = Thread.currentThread().stackTrace.getOrNull(2)?.className ?: "UnknownSource"
        return getLogger(callerClassName)
    }

    fun getDefaultLogger(vararg appenders: Appender): DefaultLogger {
        return DefaultLogger("ABC", *appenders)
    }

    /**
     * Retrieves a logger instance named after the provided class.
     *
     * @param clazz The class to name the logger after.
     * @return The Logger instance.
     */
    fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.name)
    }

    /**
     * Configures the logging system using a builder-style lambda.
     * Provides access to the central LogDispatcher for setting level, adding appenders/filters.
     * This operation should typically be done once at application startup.
     * Configuration changes are thread-safe.
     *
     * Example:
     * ```kotlin
     * LoggerFactory.configure {
     *     level = LogLevel.DEBUG
     *     addAppender(ConsoleAppender(SimpleFormatter()))
     *     addFilter(LevelFilter(LogLevel.INFO)) // Only log INFO and above globally
     * }
     * ```
     */
    fun configure(block: LogDispatcher.() -> Unit) {
        LogDispatcher.apply(block)
    }

    /**
     * Shuts down the logging system, stopping all appenders and releasing resources.
     * Call this during application shutdown if necessary (e.g., to flush file buffers).
     */
    fun shutdown() {
        LogDispatcher.shutdown()
    }
}