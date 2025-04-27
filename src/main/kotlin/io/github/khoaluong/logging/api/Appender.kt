package io.github.khoaluong.logging.api
interface Appender {
    val formatter: Formatter
    suspend fun append(event: LogEvent)

    fun start()
    suspend fun stop()
}