package io.github.khoaluong.logging.internal

import io.github.khoaluong.logging.api.*
import io.github.khoaluong.logging.coroutines.LoggingContextElement
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Internal singleton responsible for:
 * - Holding the global logging configuration (level, appenders, filters).
 * - Caching logger instances.
 * - Dispatching LogEvents to appropriate appenders after filtering.
 * - Ensuring thread safety for configuration and dispatching.
 */
object LogDispatcher : IFilterable<LogEvent> {
    // Use AtomicReference for thread-safe updates of the global level
    private val globalLevel = AtomicReference(LogLevel.TRACE) // Default level

    // Use CopyOnWriteArrayList for thread-safe iteration and modification (good for read-heavy appenders/filters)
    private val appenders = CopyOnWriteArrayList<Appender>()
    override val filters = CopyOnWriteArrayList<Filter>()

    // Use ConcurrentHashMap for thread-safe logger caching
    private val loggerCache = ConcurrentHashMap<String, Logger>()

    // Ensure DefaultLogger is created only once per name
    fun getLogger(name: String): Logger {
        return loggerCache.computeIfAbsent(name) { loggerName ->
            DefaultLogger(loggerName)
        }
    }

    // --- Configuration Methods (called via LoggerFactory.configure) ---

    var level: LogLevel
        get() = globalLevel.get()
        set(value) {
            globalLevel.set(value)
            // Optional: Clear logger cache if level changes affect isEnabled checks significantly?
            // loggerCache.clear() // Might be too aggressive
        }

    fun addAppender(appender: Appender) {
        appenders.add(appender)
    }

    fun removeAppender(appender: Appender) {
        appenders.remove(appender)
    }

    fun clearAppenders() {
        appenders.forEach { it.stop() } // Stop before clearing
        appenders.clear()
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

    // --- Event Dispatching ---

    /**
     * Processes a log event generated by a logger.
     * 1. Checks global log level.
     * 2. Applies all registered filters.
     * 3. If not filtered out, dispatches the event to all registered appenders.
     */
    suspend fun dispatch(event: LogEvent) {
        // 1. Check global level first (cheap check)
        if (!event.level.isLogLevelEnabled(globalLevel.get())) {
            return
        }

        // Enrich with coroutine context if available
        val enrichedEvent = enrichWithCoroutineContext(event)

        // 2. Apply filters
        // Use all {} for efficiency (stops on first false)
        if (!filterAll(enrichedEvent)) {
            return // Filtered out
        }

        // 3. Dispatch to appenders (iteration is thread-safe)
        appenders.forEach { appender ->
            try {
                // Basic level check per appender could be added here if needed
                // if(event.level.isEnabled(appender.minimumLevel)) { ... }
                appender.append(enrichedEvent)
            } catch (e: Exception) {
                // Handle errors during appending (e.g., log to stderr)
                System.err.println("ERROR: Failed to append log event via ${appender::class.simpleName}: ${e.message}")
                e.printStackTrace(System.err)
            }
        }
    }

    private suspend fun enrichWithCoroutineContext(event: LogEvent): LogEvent {
        // Only attempt enrichment if running inside a coroutine
        // Checking for Job presence is a decent heuristic, though not foolproof.
        // A dedicated marker element might be more robust if strictly needed.
        return try {
            val context = currentCoroutineContext()
            if (context[kotlinx.coroutines.Job] != null) { // Check if we are in a coroutine scope
                val loggingContext = context[LoggingContextElement.Key]
                if (loggingContext != null && loggingContext.data.isNotEmpty()) {
                    event.copy(contextData = event.contextData + loggingContext.data) // Merge maps
                } else {
                    event
                }
            } else {
                event // Not in a coroutine or no Job found
            }
        } catch (e: IllegalStateException) {
            // currentCoroutineContext() throws if not called from a coroutine
            event // Not in a coroutine
        }
    }


    /**
     * Stops all registered appenders.
     */
    fun shutdown() {
        clearAppenders() // Stops and clears
        loggerCache.clear() // Clear cache on shutdown
        // Reset level? Optional.
        // globalLevel.set(LogLevel.INFO)
    }

    // --- Internal Access for DefaultLogger ---
    fun isEnabled(level: LogLevel): Boolean {
        return level.isLogLevelEnabled(globalLevel.get())
    }
}