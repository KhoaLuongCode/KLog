package io.github.khoaluong.logging.internal.filters

import io.github.khoaluong.logging.api.Filter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.api.LogLevel

/**
 * A filter that allows log events only if their level is greater than or equal to
 * a specified minimum level.
 *
 * @param minLevel The minimum log level to allow. Events with this level or higher will pass.
 */
class LevelFilter(private val minLevel: LogLevel) : Filter {

    /**
     * Returns `true` if the event's level is at or above the configured minimum level.
     */
    override fun filter(event: LogEvent): Boolean {
        return event.level.isLogLevelEnabled(minLevel) // Use LogLevel's comparison logic
    }

    override fun toString(): String {
        return "LevelFilter(minLevel=$minLevel)"
    }
}