package io.github.khoaluong.logging.api

fun interface Filter {
    fun filter(event: LogEvent): Boolean
}