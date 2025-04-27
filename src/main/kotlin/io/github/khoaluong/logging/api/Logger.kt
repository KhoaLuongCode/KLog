package io.github.khoaluong.logging.api

import io.github.khoaluong.logging.internal.filters.LevelFilter
import kotlinx.coroutines.CoroutineScope

abstract class Logger : IFilterable<LogEvent> {
    abstract val name: String
    abstract val scope: CoroutineScope
    abstract val levelFilter: LevelFilter
    abstract val loggerID: String

    /** Generic log method */
    abstract suspend fun log(level: LogLevel, throwable: Throwable? = null, message: () -> Any?)

    /** Log a TRACE message */
    abstract suspend fun trace(throwable: Throwable? = null, message: () -> Any?)
    suspend fun trace(message: () -> Any?) = trace(null, message)
    suspend fun trace(message: String) = trace { message }
    suspend fun trace(throwable: Throwable?, message: String) = trace(throwable) { message }

    /** Log a DEBUG message */
    abstract suspend fun debug(throwable: Throwable? = null, message: () -> Any?) //
    suspend fun debug(message: () -> Any?) = debug(null, message)
    suspend fun debug(message: String) = debug { message }
    suspend fun debug(throwable: Throwable?, message: String) = debug(throwable) { message }

    /** Log an INFO message */
    abstract suspend fun info(throwable: Throwable? = null, message: () -> Any?)
    suspend fun info(message: () -> Any?) = info(null, message)
    suspend fun info(message: String) = info { message }
    suspend fun info(throwable: Throwable?, message: String) = info(throwable) { message }

    /** Log a WARN message */
    abstract suspend fun warn(throwable: Throwable? = null, message: () -> Any?)
    suspend fun warn(message: () -> Any?) = warn(null, message)
    suspend fun warn(message: String) = warn { message }
    suspend fun warn(throwable: Throwable?, message: String) = warn(throwable) { message }

    /** Log an ERROR message */
    abstract suspend fun error(throwable: Throwable? = null, message: () -> Any?)
    suspend fun error(message: () -> Any?) = error(null, message)
    suspend fun error(message: String) = error { message }
    suspend fun error(throwable: Throwable?, message: String) = error(throwable) { message }

    abstract suspend fun shutdown()

    // IFilterable methods remain non-suspend as they configure the logger state
    abstract override val filters: List<Filter>
    abstract override fun addFilter(filter: Filter)
    abstract override fun addFilters(vararg filters: Filter)
    abstract override fun addFilters(filters: Iterable<Filter>)
    abstract override fun removeFilter(filter: Filter)
    abstract override fun filterAll(event: LogEvent): Boolean
}