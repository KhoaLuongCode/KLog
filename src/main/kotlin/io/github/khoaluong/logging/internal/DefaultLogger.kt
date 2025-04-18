package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import kotlinx.coroutines.*
// Explicit import needed if LogLevel methods used directly without LogDispatcher check
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
class DefaultLogger(
    override val name: String, vararg val appenders: Appender
) : Logger, IFilterable<LogEvent> {
    override val supervisor: Job = SupervisorJob()
    override val scope: CoroutineScope = CoroutineScope(CoroutineName(name) + Dispatchers.Default + supervisor)
    override val filters: MutableList<Filter> = mutableListOf()

    init {
        appenders.forEach {
            it.start()
        }
    }

    override fun isEnabled(level: LogLevel): Boolean {
        // Check against the dispatcher's configured global level
        return LogDispatcher.isEnabled(level)
    }

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
                coroutineContext = scope.coroutineContext[CoroutineName]?.name ?: "UnknownCoroutine",
                throwable = throwable
                // contextData is added by the dispatcher
            )
            if (!filterAll(event)) return
            scope.launch {
                appenders.forEach { appender ->
                    appender.append(event)
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
    override fun shutdown() {
        appenders.forEach {
            it.stop()
        }
    }

    override fun addFilter(filter: Filter) {
        filters.add(filter)
    }

    override fun addFilters(vararg filters: Filter) {
        filters.forEach { addFilter(it) }
    }

    override fun removeFilter(filter: Filter) {
        filters.remove(filter)
    }

    override fun filterAll(event: LogEvent): Boolean {
        if (filters.isEmpty()) {
            return true
        }
        return filters.all { it.filter(event) }
    }
}