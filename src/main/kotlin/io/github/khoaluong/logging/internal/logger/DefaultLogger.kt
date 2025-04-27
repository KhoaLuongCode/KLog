package io.github.khoaluong.logging.internal.logger

import io.github.khoaluong.logging.api.*
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.filters.LevelFilter
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.*
import java.time.Instant
import kotlin.coroutines.coroutineContext

open class DefaultLogger(
    override val name: String,
    override val levelFilter: LevelFilter = LevelFilter(LogLevel.INFO),
    filterList: List<Filter>,
    vararg val appenders: Appender,
) : Logger() {
    override val scope: CoroutineScope = LogDispatcher.scope
    override val filters: MutableList<Filter> = mutableListOf()
    override val loggerID: String = java.util.UUID.randomUUID().toString()
    val jobs = mutableListOf<Job>()

    init {
        filters.add(levelFilter)
        filters.addAll(filterList)
        appenders.forEach {
            it.start()
            LogDispatcher.registerAppender(it)
        }
    }


    override suspend fun log(level: LogLevel, throwable: Throwable?, message: () -> Any?) {

        val msgString = message()?.toString() ?: "null"

        val callerContext = coroutineContext
        val coroutineName = callerContext[CoroutineName]?.name ?: ""


        val event = LogEvent(
            timestamp = Instant.now(),
            level = level,
            loggerName = name,
            threadName = Thread.currentThread().name,
            message = msgString,
            coroutineContext = coroutineName,
            throwable = throwable
        )
        if (!filterAll(event)) return
        jobs.add(scope.launch {
            appenders.forEach { appender ->
                appender.append(event)
            }
        })

    }

    override suspend fun trace(throwable: Throwable?, message: () -> Any?) = log(LogLevel.TRACE, throwable, message)
    override suspend fun debug(throwable: Throwable?, message: () -> Any?) = log(LogLevel.DEBUG, throwable, message)
    override suspend fun info(throwable: Throwable?, message: () -> Any?) = log(LogLevel.INFO, throwable, message)
    override suspend fun warn(throwable: Throwable?, message: () -> Any?) = log(LogLevel.WARN, throwable, message)
    override suspend fun error(throwable: Throwable?, message: () -> Any?) = log(LogLevel.ERROR, throwable, message)
    override suspend fun shutdown() {
        val jobsToJoin = synchronized(jobs) { jobs.toList() }
        jobsToJoin.joinAll()
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