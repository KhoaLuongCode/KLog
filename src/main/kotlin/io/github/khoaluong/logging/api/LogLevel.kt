package io.github.khoaluong.logging.api

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    fun isLogLevelEnabled(minLevel: LogLevel): Boolean {
        return this.ordinal >= minLevel.ordinal
    }
}