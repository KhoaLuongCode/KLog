package io.github.khoaluong.logging.internal.filters

import io.github.khoaluong.logging.api.Filter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.api.LogLevel

class LevelFilter(private val minLevel: LogLevel) : Filter {

    override fun filter(event: LogEvent): Boolean {
        return event.level.isLogLevelEnabled(minLevel)
    }

    override fun toString(): String {
        return "LevelFilter(minLevel=$minLevel)"
    }
}