package io.github.khoaluong.logging
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

fun highConcurrency() = runBlocking {

    val cnt = AtomicInteger(0)
    repeat(10){
        launch {
            val logger = LoggerFactory.getConsoleLogger(name = "Logger${cnt.incrementAndGet()}")
            repeat(100){
                logger.info { "Hello World $it" }
            }
        }
    }
    //val logger = LoggerFactory.getDefaultLogger(name = "Logger${1}")


}
