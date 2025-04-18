package io.github.khoaluong.logging.io

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import java.io.ByteArrayOutputStream
import java.io.OutputStream

data class ChannelWriter(
    val channel: Channel<String>,
    val outputStream: OutputStream,
    val id: String,
    var running: Boolean = false,
    var job: Job? = null
)
