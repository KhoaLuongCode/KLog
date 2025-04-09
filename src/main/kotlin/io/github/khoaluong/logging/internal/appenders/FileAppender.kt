package io.github.khoaluong.logging.internal.appenders

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.Formatter
import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.internal.formatters.SimpleFormatter
import java.io.BufferedWriter
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
class FileAppender(
    private val filePath: String,
    override val formatter: Formatter = SimpleFormatter(),
    private val createDirs: Boolean = true
) : Appender {

    private var writer: BufferedWriter? = null
    private val lock = ReentrantLock() // Lock for thread-safe file access

    init {
        try {
            val path = Paths.get(filePath)
            if (createDirs) {
                Files.createDirectories(path.parent)
            }
            // Open in append mode, create if not exists
            val fileWriter = FileWriter(filePath, StandardCharsets.UTF_8, true) // Append mode
            writer = BufferedWriter(fileWriter)
        } catch (e: IOException) {
            System.err.println("ERROR: Failed to initialize FileAppender for path '$filePath': ${e.message}")
            writer = null // Ensure writer is null if initialization failed
        }
    }

    override fun append(event: LogEvent) {
        val currentWriter = writer ?: return // Don't log if writer init failed

        try {
            val formattedMessage = formatter.format(event)
            lock.withLock { // Ensure only one thread writes at a time
                currentWriter.write(formattedMessage)
                currentWriter.newLine()
                currentWriter.flush() // Flush buffer to ensure message is written
            }
        } catch (e: Exception) { // Catch potential formatting or IO errors
            System.err.println("ERROR: FileAppender failed to write to '$filePath': ${e.message}")
            // Consider stopping the appender or trying to reopen the file on certain errors
        }
    }

    /**
     * Closes the underlying file writer.
     */
    override fun stop() {
        lock.withLock {
            try {
                writer?.close()
            } catch (e: IOException) {
                System.err.println("ERROR: Failed to close FileAppender writer for '$filePath': ${e.message}")
            } finally {
                writer = null
            }
        }
    }

    override fun toString(): String {
        return "FileAppender(filePath='$filePath', formatter=${formatter::class.simpleName})"
    }
}