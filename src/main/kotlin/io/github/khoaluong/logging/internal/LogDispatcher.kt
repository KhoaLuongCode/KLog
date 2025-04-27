package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import io.github.khoaluong.logging.io.KLogWriter // Assuming KLogWriter handles actual writing
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object LogDispatcher {
    private val globalLevel = AtomicReference(LogLevel.INFO)
    private val loggerCache = ConcurrentHashMap<String, Logger>()
    private val registeredAppender = ConcurrentHashMap.newKeySet<Appender>()

    val supervisorJob = SupervisorJob()

    fun getLogger(name: String): Logger? {
        return loggerCache[name]
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
                        System.err.println("ERROR during logger shutdown (${logger.name}): ${e.message}")
                        e.printStackTrace(System.err)
                    }
                }
            }
        }
        println("LogDispatcher: Loggers finished internal jobs.")
        loggerCache.clear()


        registeredAppender.forEach { appender ->
            try {
                appender.stop()
            } catch (e: Exception) {
                System.err.println("ERROR stopping appender '${appender::class.simpleName}': ${e.message}")
                e.printStackTrace(System.err)
            }
        }
        println("LogDispatcher: Appenders stopped.")
        registeredAppender.clear()

        supervisorJob.cancel()
        println("LogDispatcher: Supervisor job cancelled.")
    }

    fun isEnabled(level: LogLevel): Boolean {
        return level.isLogLevelEnabled(globalLevel.get())
    }
}