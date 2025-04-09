package io.github.khoaluong.logging.internal.formatters

import io.github.khoaluong.logging.api.LogEvent
import io.github.khoaluong.logging.api.LogLevel
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.test.assertTrue

class SimpleFormatterTest {

    @Test
    fun `format produces expected string format`() {
        val fixedInstant = Instant.parse("2023-10-27T10:15:30.123Z")
        val zoneId = ZoneId.of("UTC") // Use fixed zone for consistent test
        val formatter = SimpleFormatter(zoneId = zoneId)
        val event = LogEvent(
            timestamp = fixedInstant,
            level = LogLevel.INFO,
            loggerName = "MyLogger",
            threadName = "main",
            message = "Hello World",
            contextData = mapOf("user" to "tester")
        )

        val expectedTimestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(zoneId)
            .format(fixedInstant)

        val formatted = formatter.format(event)

        assertTrue(formatted.startsWith("[$expectedTimestamp]"))
        assertTrue(formatted.contains("[INFO ]")) // Padded
        assertTrue(formatted.contains("[main]"))
        assertTrue(formatted.contains("MyLogger - Hello World"))
        assertTrue(formatted.contains("{user=tester}")) // Context data
    }

    @Test
    fun `format includes stack trace for throwable`() {
        val formatter = SimpleFormatter()
        val exception = RuntimeException("Test Exception")
        val event = LogEvent(
            level = LogLevel.ERROR,
            loggerName = "ErrorLogger",
            message = "An error occurred",
            throwable = exception
        )

        val formatted = formatter.format(event)

        assertTrue(formatted.contains("An error occurred"))
        assertTrue(formatted.contains("java.lang.RuntimeException: Test Exception"))
        assertTrue(formatted.contains("at io.github.khoaluong.logging.internal.formatters.SimpleFormatterTest")) // Stack trace element
    }
}