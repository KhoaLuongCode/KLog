package io.github.khoaluong.logging.internal.appenders

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import io.github.khoaluong.logging.io.KLogWriter
import kotlinx.coroutines.channels.Channel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FileAppender private constructor(
    private val filePath: String,
    override val formatter: Formatter = SimpleFormatter(),
    private val id: String,
    private val channel: Channel<String>
) : Appender {


    companion object {
        private val appenders = mutableMapOf<String, FileAppender>()

        fun createFileAppender(
            filePath: String,
            formatter: Formatter = SimpleFormatter(),
        ): FileAppender {
            for (f in appenders.values) {
                if (f.filePath == filePath) {
                    return f
                }
            }
            if (!File(filePath).exists() || !appenders.contains(filePath.hashCode().toString())) {
                try {
                    val outputStream = Files.newOutputStream(
                        Paths.get(filePath),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                    )
                    val id = java.util.UUID.randomUUID().toString()
                    val channel = KLogWriter.createWriter(
                        id, 1000, outputStream
                    )
                    val appender = FileAppender(filePath, formatter, id, channel)
                    appenders[filePath.hashCode().toString()] = appender
                    return appender
                } catch (e: IOException) {
                    System.err.println("ERROR: Failed to create file at '$filePath': ${e.message}")
                    throw e
                }
            }
            return appenders.values.firstOrNull { it.filePath == filePath }
                ?: throw IllegalStateException("FileAppender for '$filePath' not found.")
        }
    }


    override suspend fun append(event: LogEvent) {
        try {
            val formattedMessage = formatter.format(event)
            channel.send(formattedMessage)
        } catch (e: Exception) {
            System.err.println("ERROR: FileAppender failed to write to '$filePath': ${e.message}")
        }
    }

    override fun start() {
        KLogWriter.startWriter(id)
    }

    override suspend fun stop() {
        KLogWriter.stopWriter(id)

    }

    override fun toString(): String {
        return "FileAppender(filePath='$filePath', formatter=${formatter::class.simpleName})"
    }
}