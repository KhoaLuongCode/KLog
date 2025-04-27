package io.github.khoaluong.logging.internal.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

data class LogContextSnapshot(
    val coroutineName: String?,
    val contextData: Map<String, String>?
)

internal val logContextThreadLocal = ThreadLocal<LogContextSnapshot?>()

class CoroutineLoggingContext : ThreadContextElement<LogContextSnapshot?> {

    companion object Key : CoroutineContext.Key<CoroutineLoggingContext>

    override val key: CoroutineContext.Key<*>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): LogContextSnapshot? {
        val oldState = logContextThreadLocal.get()
        val currentName = context[CoroutineName]?.name
        val newState = LogContextSnapshot(currentName, null /* currentData */)
        logContextThreadLocal.set(newState)
        return oldState
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: LogContextSnapshot?) {
        logContextThreadLocal.set(oldState)
    }
}