package io.github.khoaluong.logging.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Provides helper functions for integrating logging context with Kotlin Coroutines.
 */
object CoroutineSupport {

    /**
     * Executes a suspending block within a coroutine scope that includes
     * the specified logging context data. This data will be available
     * via `coroutineContext[LoggingContextElement.Key]` within the block
     * and automatically picked up by the logging framework if configured.
     *
     * Example:
     * ```kotlin
     * CoroutineScope(Dispatchers.Default).launch {
     *     withLoggingContext("userId" to "user123", "requestId" to "req-abc") {
     *         logger.info("Processing request") // Log will include userId and requestId
     *         delay(100)
     *         anotherSuspendingFunction() // Context is propagated
     *     }
     *     logger.info("Processing finished") // Log will *not* include the context data
     * }
     * ```
     *
     * @param contextData Map of key-value pairs to add to the logging context.
     * @param block The suspending lambda function to execute with the added context.
     * @return The result of the block execution.
     */
    suspend fun <T> withLoggingContext(
        contextData: Map<String, String>,
        block: suspend CoroutineScope.() -> T
    ): T {
        if (contextData.isEmpty()) {
            // No context to add, just run the block
            return CoroutineScope(coroutineContext).block()
        }

        // Get the current logging context element, if any
        val currentElement = coroutineContext[LoggingContextElement.Key]
        // Create the new element, merging with existing data if present
        val newElement = LoggingContextElement(
            // New data takes precedence over existing data for the same keys
            (currentElement?.data ?: emptyMap()) + contextData
        )

        // Run the block with the new context element added
        return withContext(newElement) {
            block()
        }
    }

    /**
     * Convenience function to add a single key-value pair to the logging context.
     * @see withLoggingContext
     */
    suspend fun <T> withLoggingContext(
        key: String,
        value: String,
        block: suspend CoroutineScope.() -> T
    ): T = withLoggingContext(mapOf(key to value), block)

    /**
     * Retrieves the current logging context data from the coroutine context, if available.
     * Returns an empty map if no LoggingContextElement is found.
     *
     * Note: This is primarily for internal use by the logger/dispatcher but can be used externally.
     *
     * @return The current map of logging context data.
     */
    suspend fun getCurrentLoggingContext(): Map<String, String> {
        return coroutineContext[LoggingContextElement.Key]?.data ?: emptyMap()
    }

    // Alternative approach using ThreadContextElement for automatic restoration (more complex)
    // class LoggingContextElementUpdater(private val newData: Map<String, String>) : ThreadContextElement<Map<String, String>?> {
    //     private var oldData: Map<String, String>? = null
    //
    //     override val key: CoroutineContext.Key<*> = LoggingContextElement.Key
    //
    //     override fun updateThreadContext(context: CoroutineContext): Map<String, String>? {
    //         val currentElement = context[LoggingContextElement.Key]
    //         oldData = currentElement?.data // Store old state
    //         // How to update the element in the context itself? This is tricky with immutable context.
    //         // This approach might require thread-local storage or a different design.
    //         // Stick with withContext approach for simplicity and correctness.
    //         return oldData
    //     }
    //
    //     override fun restoreThreadContext(context: CoroutineContext, oldState: Map<String, String>?) {
    //         // Restore the previous state? How?
    //     }
    // }
}