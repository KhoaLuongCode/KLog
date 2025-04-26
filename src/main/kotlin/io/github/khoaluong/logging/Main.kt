package io.github.khoaluong.logging

import io.github.khoaluong.logging.internal.LogDispatcher
import kotlinx.coroutines.runBlocking

fun main():Unit =runBlocking {
    highConcurrency()
    //val logger = LoggerFactory.getConsoleLogger(name = "Logger")
    //logger.info { "Hello World" }
    LogDispatcher.shutdown()
}

