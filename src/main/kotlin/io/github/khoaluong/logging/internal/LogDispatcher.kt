package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object LogDispatcher {
    // Use AtomicReference for thread-safe updates of the global level
    private val globalLevel = AtomicReference(LogLevel.TRACE) // Default level
    private val loggerCache = ConcurrentHashMap<String, Logger>()

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Ensure DefaultLogger is created only once per name
    fun getLogger(name: String): Logger? {
        return loggerCache.get(name)
    }

    // --- Configuration Methods (called via LoggerFactory.configure) ---

    var level: LogLevel
        get() = globalLevel.get()
        set(value) {
            globalLevel.set(value)
            // Optional: Clear logger cache if level changes affect isEnabled checks significantly?
            // loggerCache.clear() // Might be too aggressive
        }

    init {
        level = LogLevel.INFO // Set default level
    }

    fun registerLogger(logger: Logger) {
        loggerCache.putIfAbsent(logger.loggerID, logger)
    }

    suspend fun shutdown()  {
        loggerCache.values.forEach{
            it.shutdown()
        }
    }


    // --- Internal Access for DefaultLogger ---
    fun isEnabled(level: LogLevel): Boolean {
        return level.isLogLevelEnabled(globalLevel.get())
    }
}