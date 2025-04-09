package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.LogEvent
// Explicit import needed if LogLevel methods used directly without LogDispatcher check
import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.api.Logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Default implementation of the Logger interface.
 * It checks if a level is enabled before creating and dispatching the LogEvent.
 * Dispatches events asynchronously using GlobalScope by default.
 * Consider providing a configurable CoroutineScope later.
 *
 * @param name The name of this logger.
 * @param dispatcher The central dispatcher to send log events to.
 */
internal class DefaultLogger(
    override val name: String,
    private val dispatcher: LogDispatcher
) : Logger {

    override fun isEnabled(level: LogLevel): Boolean {
        // Check against the dispatcher's configured global level
        return dispatcher.isEnabled(level)
    }

    @OptIn(DelicateCoroutinesApi::class) // Using GlobalScope for fire-and-forget logging
    override fun log(level: LogLevel, throwable: Throwable?, message: () -> Any?) {
        if (isEnabled(level)) {
            // Evaluate the message lambda only if the level is enabled
            val msgString = message()?.toString() ?: "null"

            val event = LogEvent(
                timestamp = Instant.now(),
                level = level,
                loggerName = name,
                threadName = Thread.currentThread().name, // Captured here
                message = msgString,
                throwable = throwable
                // contextData is added by the dispatcher
            )

            // Dispatch asynchronously to avoid blocking the calling thread
            // Using GlobalScope is simple but has drawbacks (unstructured concurrency).
            // A dedicated logging scope or allowing user-provided scope would be better.
            GlobalScope.launch {
                try {
                    dispatcher.dispatch(event)
                } catch (e: Exception) {
                    // Should not happen if dispatcher handles errors, but good practice
                    System.err.println("ERROR: Uncaught exception during log dispatch: ${e.message}")
                    e.printStackTrace(System.err)
                }
            }
        }
    }

    // --- Level Specific Methods ---

    override fun trace(throwable: Throwable?, message: () -> Any?) = log(LogLevel.TRACE, throwable, message)
    override fun debug(throwable: Throwable?, message: () -> Any?) = log(LogLevel.DEBUG, throwable, message)
    override fun info(throwable: Throwable?, message: () -> Any?) = log(LogLevel.INFO, throwable, message)
    override fun warn(throwable: Throwable?, message: () -> Any?) = log(LogLevel.WARN, throwable, message)
    override fun error(throwable: Throwable?, message: () -> Any?) = log(LogLevel.ERROR, throwable, message)
}