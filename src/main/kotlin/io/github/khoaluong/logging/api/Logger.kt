package io.github.khoaluong.logging.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * The core interface for logging messages.
 * Provides methods for different log levels.
 */
interface Logger {
    /**
     * The name associated with this logger instance.
     */
    val name: String
    val scope: CoroutineScope
    val supervisor: Job

    /**
     * Checks if a specific log level is currently enabled for this logger.
     * Can be used for performance optimization to avoid expensive message creation.
     *
     * @param level The log level to check.
     * @return `true` if the level is enabled, `false` otherwise.
     */
    fun isEnabled(level: LogLevel): Boolean

    /** Generic log method */
    fun log(level: LogLevel, throwable: Throwable? = null, message: () -> Any?)

    /** Log a TRACE message */
    fun trace(throwable: Throwable? = null, message: () -> Any?)
    fun trace(message: () -> Any?) = trace(null, message)
    fun trace(message: String) = trace { message }
    fun trace(throwable: Throwable?, message: String) = trace(throwable) { message }

    /** Log a DEBUG message */
    fun debug(throwable: Throwable? = null, message: () -> Any?)
    fun debug(message: () -> Any?) = debug(null, message)
    fun debug(message: String) = debug { message }
    fun debug(throwable: Throwable?, message: String) = debug(throwable) { message }

    /** Log an INFO message */
    fun info(throwable: Throwable? = null, message: () -> Any?)
    fun info(message: () -> Any?) = info(null, message)
    fun info(message: String) = info { message }
    fun info(throwable: Throwable?, message: String) = info(throwable) { message }

    /** Log a WARN message */
    fun warn(throwable: Throwable? = null, message: () -> Any?)
    fun warn(message: () -> Any?) = warn(null, message)
    fun warn(message: String) = warn { message }
    fun warn(throwable: Throwable?, message: String) = warn(throwable) { message }

    /** Log an ERROR message */
    fun error(throwable: Throwable? = null, message: () -> Any?)
    fun error(message: () -> Any?) = error(null, message)
    fun error(message: String) = error { message }
    fun error(throwable: Throwable?, message: String) = error(throwable) { message }

    fun shutdown()
}