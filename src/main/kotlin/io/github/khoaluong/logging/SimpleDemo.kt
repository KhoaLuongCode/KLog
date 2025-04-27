package io.github.khoaluong.logging
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import io.github.khoaluong.logging.internal.appenders.ConsoleAppender
import io.github.khoaluong.logging.internal.appenders.FileAppender
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import io.github.khoaluong.logging.internal.formatters.CsvFormatter
import io.github.khoaluong.logging.internal.formatters.JsonFormatter
import kotlinx.coroutines.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
fun main(): Unit = runBlocking(CoroutineName("main-runBlocking")) {
    println("--- Starting Logging Sample ---")
    println(LogDispatcher.supervisorJob)
    val logger = try {
        LoggerFactory.getLogger(
            name = "SampleApp",
            appenders = arrayOf(
                // Console output
                ConsoleAppender.createConsoleAppender(target = ConsoleAppender.ConsoleTarget.STDOUT),

                // File output SimpleFormatter
                FileAppender.createFileAppender("sample.log", formatter = SimpleFormatter()),

                // File output CsvFormatter
                FileAppender.createFileAppender("sample.csv", formatter = CsvFormatter()),

                // File output JsonFormatter
                FileAppender.createFileAppender("sample.json", formatter = JsonFormatter())
            )
        ) ?: LoggerFactory.getDefaultLogger()
    } catch (e: IOException) {
        System.err.println("ERROR: Failed to create one or more file appenders: ${e.message}")
        LoggerFactory.getConsoleLogger(name = "SampleApp-Fallback")
    }


    logger.trace("This trace message will NOT be logged (default level is INFO).")
    logger.debug("Configuration complete. Starting operations.")
    logger.info("This is an informational message.")
    logger.warn("This is a warning message.")
    logger.error("This is an error message.")

    // Log with an exception
    logger.error(RuntimeException("Something went wrong!"), "An error occurred during processing.")

    // Log using a lambda
    logger.info { "This uses a lambda for potentially expensive message creation." }
    println(logger.scope.coroutineContext.job.parent) // Just showcase how to access the parent

    println("--- Log messages sent. Check console output, sample.log, sample.csv, and sample.json ---")
    println("--- Waiting for logs to be written... ---")


    println("--- Shutting down LogDispatcher ---")
    LogDispatcher.shutdown()
    println("--- Logging Sample Complete ---")

}