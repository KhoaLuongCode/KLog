package io.github.khoaluong.logging.api

fun interface Formatter {
    fun format(event: LogEvent): String
}