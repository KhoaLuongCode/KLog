package io.github.khoaluong.logging

import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import io.github.khoaluong.logging.internal.appenders.ConsoleAppender
import io.github.khoaluong.logging.internal.appenders.FileAppender // Optional
import io.github.khoaluong.logging.internal.filters.LevelFilter
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// Get a logger for this file/application
// Using the extension function for top-level logger
// Or: private val logger = LoggerFactory.getLogger("SampleApp")

fun main(): Unit = runBlocking { // Use runBlocking for simple synchronous-like execution
    println("--- Starting Logging Sample ---")

    // --- Configuration ---
    // This should ideally happen only once at application startup

    val logger = LoggerFactory.getLogger(
        name = "SampleApp", appenders = arrayOf(
            ConsoleAppender.createConsoleAppender(target = ConsoleAppender.ConsoleTarget.STDOUT),
            FileAppender.createFileAppender("sample.log", formatter = SimpleFormatter())
        )
    ) ?: LoggerFactory.getDefaultLogger()


    logger.trace("This trace message will NOT be logged (default level is INFO or DEBUG).") // Will be filtered by level
    logger.debug("Configuration complete. Starting operations.") // Will be logged if level is DEBUG or TRACE
    logger.info("This is an informational message.") // Will be logged
    logger.warn("This is a warning message.")       // Will be logged
    logger.error("This is an error message.")       // Will be logged
    logger.info { "This uses a lambda for potentially expensive message creation." }






    LogDispatcher.shutdown()
}
