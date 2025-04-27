package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object LogDispatcher {
    private val globalLevel = AtomicReference(LogLevel.TRACE) // Default level
    private val loggerCache = ConcurrentHashMap<String, Logger>()


    val supervisorJob = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default)
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
    }


    suspend fun shutdown() {
        loggerCache.values.forEach {
            it.shutdown()
        }
    }


    fun isEnabled(level: LogLevel): Boolean {
        return level.isLogLevelEnabled(globalLevel.get())
    }
}
