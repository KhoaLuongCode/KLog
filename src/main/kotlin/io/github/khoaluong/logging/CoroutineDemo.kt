package io.github.khoaluong.logging
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import kotlinx.coroutines.*

fun main(): Unit = runBlocking(CoroutineName("main-runBlocking")) {

    val logger = LoggerFactory.getConsoleLogger(name = "MyApp")

    logger.info("Starting up in main context.")

    launch(CoroutineName("task-1")) {
        logger.info("Running task 1.")
        delay(10)
        logger.debug("Task 1 finishing.")
    }

    withContext(Dispatchers.IO + CoroutineName("io-operation")) {
        logger.warn("Performing I/O.")
    }

    logger.error("Shutting down.")

    LogDispatcher.shutdown()
}