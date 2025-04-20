package io.github.khoaluong.logging.internal.logger

import io.github.khoaluong.logging.api.*
import io.github.khoaluong.logging.internal.filters.LevelFilter
import kotlinx.coroutines.*
// Explicit import needed if LogLevel methods used directly without LogDispatcher check
import java.time.Instant


open class DefaultLogger(
    override val name: String,
    override val levelFilter: LevelFilter = LevelFilter(LogLevel.INFO),
    filterList: List<Filter>,
    vararg val appenders: Appender,
) : Logger() {
    override val supervisor: Job = SupervisorJob()
    override val scope: CoroutineScope = CoroutineScope(CoroutineName(name) + Dispatchers.Default + supervisor)
    override val filters: MutableList<Filter> = mutableListOf()
    override val loggerID: String = java.util.UUID.randomUUID().toString()

    init {
        filters.add(levelFilter)
        filters.addAll(filterList)
        appenders.forEach {
            it.start()
        }
    }


    override suspend fun log(level: LogLevel, throwable: Throwable?, message: () -> Any?) {

        val msgString = message()?.toString() ?: "null"

        val event = LogEvent(
            timestamp = Instant.now(),
            level = level,
            loggerName = name,
            threadName = Thread.currentThread().name, // Captured here
            message = msgString,
            coroutineContext = scope.coroutineContext[CoroutineName]?.name ?: "UnknownCoroutine",
            throwable = throwable
        )
        if (!filterAll(event)) return
        scope.launch {
            appenders.forEach { appender ->
                appender.append(event)
            }
        }.join()


    }

    // --- Level Specific Methods ---

    override suspend fun trace(throwable: Throwable?, message: () -> Any?) = log(LogLevel.TRACE, throwable, message)
    override suspend fun debug(throwable: Throwable?, message: () -> Any?) = log(LogLevel.DEBUG, throwable, message)
    override suspend fun info(throwable: Throwable?, message: () -> Any?) = log(LogLevel.INFO, throwable, message)
    override suspend fun warn(throwable: Throwable?, message: () -> Any?) = log(LogLevel.WARN, throwable, message)
    override suspend fun error(throwable: Throwable?, message: () -> Any?) = log(LogLevel.ERROR, throwable, message)
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

    override fun addFilters(filters: Iterable<Filter>) {
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