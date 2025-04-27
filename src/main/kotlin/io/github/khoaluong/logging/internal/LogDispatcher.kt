package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import io.github.khoaluong.logging.internal.logger.DefaultLogger
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object LogDispatcher {
    private val globalLevel = AtomicReference(LogLevel.TRACE) // Default level
    private val loggerCache = ConcurrentHashMap<String, Logger>()
    private val registeredAppender = ConcurrentHashMap.newKeySet<Appender>()

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun getLogger(name: String): Logger? {
        return loggerCache.get(name)
    }

    var level: LogLevel
        get() = globalLevel.get()
        set(value) {
            globalLevel.set(value)
        }

    init {
        level = LogLevel.INFO
    }

    fun registerLogger(logger: Logger) {
        loggerCache.putIfAbsent(logger.loggerID, logger)
        if (logger is DefaultLogger) {
            registeredAppender.addAll(logger.appenders)
        }
    }

    fun registerAppender(appender: Appender) {
        registeredAppender.add(appender)
    }

    suspend fun shutdown() {
        println("LogDispatcher: Shutting down...")
        coroutineScope {
            loggerCache.values.forEach { logger ->
                launch {
                    try {
                        logger.shutdown()
                    } catch (e: Exception) {
                        System.err.println("ERROR: Failed during shutdown for logger '${logger.name}': ${e.message}")
                    }
                }
            }
        }
        println("LogDispatcher: Logger internal jobs completed.")

        registeredAppender.forEach { appender ->
            try {
                appender.stop()
            } catch (e: Exception) {
                System.err.println("ERROR: Failed to stop appender '${appender::class.simpleName}': ${e.message}")
            }
        }
        println("LogDispatcher: Appenders stopped.")
        registeredAppender.clear() // Clear the set

        scope.cancel() // Cancel the supervisor job and its children
        println("LogDispatcher: Scope cancelled.")

        loggerCache.clear()
    }

    fun isEnabled(level: LogLevel): Boolean {
        return level.isLogLevelEnabled(globalLevel.get())
    }
}