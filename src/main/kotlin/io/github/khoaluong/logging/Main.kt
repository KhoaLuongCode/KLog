package io.github.khoaluong.logging

import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.internal.logger.LoggerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    highConcurrency()
}

