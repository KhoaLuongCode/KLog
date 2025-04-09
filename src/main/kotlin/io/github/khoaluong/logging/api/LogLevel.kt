package io.github.khoaluong.logging.api

/**
 * Defines the severity levels for logging events.
 * Levels are ordered from most specific (TRACE) to most general (ERROR).
 */
enum class LogLevel {
    TRACE, // Fine-grained details, typically only valuable for debugging specific parts.
    DEBUG, // Information useful for debugging the application flow.
    INFO,  // General information about application progress and state.
    WARN,  // Indicates potential problems or unusual situations.
    ERROR; // Serious errors that prevent normal operation.

    /**
     * Checks if this level is severe enough to be logged given a minimum required level.
     * @param minLevel The minimum level required for logging.
     * @return `true` if this level's ordinal is greater than or equal to `minLevel`'s ordinal.
     */
    fun isEnabled(minLevel: LogLevel): Boolean {
        return this.ordinal >= minLevel.ordinal
    }
}