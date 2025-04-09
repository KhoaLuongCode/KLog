package io.github.khoaluong.logging.coroutines

import kotlin.coroutines.CoroutineContext

/**
 * A CoroutineContext Element that holds logging-specific context data (e.g., MDC).
 * This data can be automatically included in log events generated within the coroutine's scope.
 *
 * @property data The map containing the contextual key-value pairs.
 */
data class LoggingContextElement(
    val data: Map<String, String> = emptyMap()
) : CoroutineContext.Element {

    // Define a unique Key for this element type.
    companion object Key : CoroutineContext.Key<LoggingContextElement>

    override val key: CoroutineContext.Key<*> = Key

    /**
     * Combine with another element. If it's also a LoggingContextElement, merge the data maps.
     * The data from the 'other' element takes precedence in case of key conflicts.
     */
    override fun plus(context: CoroutineContext): CoroutineContext {
        val otherElement = context[Key]
        return if (otherElement != null) {
            // Merge maps, other element's data overrides current if keys clash
            LoggingContextElement(this.data + otherElement.data)
        } else {
            // If the other context doesn't have our element, just add this one
            super.plus(context)
        }
    }
}