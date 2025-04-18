package io.github.khoaluong.logging

import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.api.Logger
import io.github.khoaluong.logging.api.LoggerFactory
import io.github.khoaluong.logging.extensions.getLogger // Use the extension
import io.github.khoaluong.logging.internal.DefaultLogger
import io.github.khoaluong.logging.internal.appenders.ConsoleAppender
import io.github.khoaluong.logging.internal.appenders.FileAppender // Optional
import io.github.khoaluong.logging.internal.filters.LevelFilter
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import io.github.khoaluong.logging.internal.formatters.JsonFormatter // Optional
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// Get a logger for this file/application
// Using the extension function for top-level logger
private val logger = getLogger()
// Or: private val logger = LoggerFactory.getLogger("SampleApp")

fun main(): Unit = runBlocking { // Use runBlocking for simple synchronous-like execution
    println("--- Starting Logging Sample ---")

    // --- Configuration ---
    // This should ideally happen only once at application startup

    val filter = LevelFilter(LogLevel.TRACE) // Filter to allow only DEBUG and higher levels


    runBlocking {
        repeat(1){
            val logger = LoggerFactory.getDefaultLogger(
                ConsoleAppender.createConsoleAppender(target = ConsoleAppender.ConsoleTarget.STDOUT),
                FileAppender.createFileAppender("sample.log", formatter = SimpleFormatter())
            ) as DefaultLogger

            logger.addFilter(filter) // Add filter to the logger
            // --- Logging Examples ---
            logger.trace("This trace message will NOT be logged (default level is INFO or DEBUG).") // Will be filtered by level
            logger.debug("Configuration complete. Starting operations.") // Will be logged if level is DEBUG or TRACE
            logger.info("This is an informational message.") // Will be logged
            logger.warn("This is a warning message.")       // Will be logged
            logger.error("This is an error message.")       // Will be logged

            logger.info { "This uses a lambda for potentially expensive message creation." }


        }
    }



    println("--- Logging examples finished. Waiting for async logs... ---")
    // Give async logging calls some time to complete before shutdown/exit
    // In a real app, you wouldn't typically sleep like this.
    delay(500)

    // --- Shutdown (Optional but recommended for FileAppender) ---
    // Releases resources like file handles

    println("--- Logging system shut down. ---")
}