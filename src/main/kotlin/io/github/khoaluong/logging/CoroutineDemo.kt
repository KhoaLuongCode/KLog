package io.github.khoaluong.logging

import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import kotlinx.coroutines.*
import io.github.khoaluong.logging.internal.coroutines.CoroutineLoggingContext

fun main(): Unit {
    runBlocking(CoroutineName("main-runBlocking") + CoroutineLoggingContext()) {

        val logger = LoggerFactory.getConsoleLogger(name = "MyApp")

        logger.info("Starting up in main context.")

        launch(CoroutineName("task-1")) {
            logger.info("Running task 1.")
            delay(100)
            logger.info("Task 1 finishing.")
        }

        withContext(Dispatchers.IO + CoroutineName("io-operation")) {
            logger.warn("Performing I/O.")
        }

        logger.error("Shutting down.")

        delay(200)
        println("Requesting LogDispatcher shutdown...")
        LogDispatcher.shutdown()
        println("LogDispatcher shutdown complete.")
    }
    println("Main finished.")
}