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

/**
 * An appender that writes formatted log events to a specified file.
 * Note: This implementation appends to a single file and does not handle rotation.
 * It uses a lock to ensure thread-safe writes.
 *
 * @param filePath The path to the log file.
 * @param formatter The formatter to use. Defaults to SimpleFormatter.
 * @param createDirs If true, attempts to create parent directories if they don't exist.
 */
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
            channel.send(formattedMessage) // Send formatted message to the channel
        } catch (e: Exception) { // Catch potential formatting or IO errors
            System.err.println("ERROR: FileAppender failed to write to '$filePath': ${e.message}")
            // Consider stopping the appender or trying to reopen the file on certain errors
        }
    }

    override fun start() {
        KLogWriter.startWriter(id)
    }

    /**
     * Closes the underlying file writer.
     */
    override fun stop() {
        KLogWriter.stopWriter(id)

    }

    override fun toString(): String {
        return "FileAppender(filePath='$filePath', formatter=${formatter::class.simpleName})"
    }
}