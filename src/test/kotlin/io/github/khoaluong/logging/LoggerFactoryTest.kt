package io.github.khoaluong.logging.io.github.khoaluong.logging

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.api.LoggerFactory
import io.github.khoaluong.logging.extensions.getLogger
import io.github.khoaluong.logging.internal.LogDispatcher
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoggerFactoryTest {

    // Reset LogDispatcher state between tests
    @BeforeEach
    @AfterEach
    fun resetDispatcher() {
        // Use reflection or a dedicated reset method if LogDispatcher is object
        try {
            val levelField = LogDispatcher::class.java.getDeclaredField("globalLevel")
            levelField.isAccessible = true
            val levelRef = levelField.get(LogDispatcher) as java.util.concurrent.atomic.AtomicReference<LogLevel>
            levelRef.set(LogLevel.INFO) // Reset to default

            val appendersField = LogDispatcher::class.java.getDeclaredField("appenders")
            appendersField.isAccessible = true
            val appendersList = appendersField.get(LogDispatcher) as java.util.concurrent.CopyOnWriteArrayList<Appender>
            appendersList.forEach { it.stop() }
            appendersList.clear()

            val filtersField = LogDispatcher::class.java.getDeclaredField("filters")
            filtersField.isAccessible = true
            val filtersList = filtersField.get(LogDispatcher) as java.util.concurrent.CopyOnWriteArrayList<*>
            filtersList.clear()

            val cacheField = LogDispatcher::class.java.getDeclaredField("loggerCache")
            cacheField.isAccessible = true
            val cacheMap = cacheField.get(LogDispatcher) as java.util.concurrent.ConcurrentHashMap<*, *>
            cacheMap.clear()

        } catch (e: Exception) {
            System.err.println("WARN: Failed to reset LogDispatcher via reflection: $e")
            // Manual reset if object has public methods
            LogDispatcher.clearAppenders()
            LogDispatcher.clearFilters()
            LogDispatcher.level = LogLevel.INFO
            // Cannot easily clear cache without reflection or internal method
        }
    }


    @Test
    fun `getLogger(name) returns a logger with the correct name`() {
        val loggerName = "TestLogger"
        val logger = LoggerFactory.getLogger(loggerName)
        assertNotNull(logger)
        assertEquals(loggerName, logger.name)
    }

    @Test
    fun `getLogger(name) returns the same instance for the same name`() {
        val loggerName = "CachedLogger"
        val logger1 = LoggerFactory.getLogger(loggerName)
        val logger2 = LoggerFactory.getLogger(loggerName)
        assertTrue { logger1 === logger2 } // Check for reference equality
    }

    @Test
    fun `getLogger(class) returns a logger named after the class`() {
        val logger = LoggerFactory.getLogger(LoggerFactoryTest::class.java)
        assertNotNull(logger)
        assertEquals(LoggerFactoryTest::class.java.name, logger.name)
    }

    @Test
    fun `getLogger() extension function returns a logger named after the class`() {
        val logger = getLogger() // Calls extension on the test class instance
        assertNotNull(logger)
        assertEquals(LoggerFactoryTest::class.java.name, logger.name)
    }

    @Test
    fun `configure changes the global log level`() {
        assertEquals(LogLevel.INFO, LogDispatcher.level) // Default
        LoggerFactory.configure {
            level = LogLevel.DEBUG
        }
        assertEquals(LogLevel.DEBUG, LogDispatcher.level)
    }

    @Test
    fun `configure adds an appender`() {
        // Check initial state (might require reflection or internal access)
        // For simplicity, just add and check if a log message goes through
        val mockAppender = mockk<Appender>(relaxed = true)
        every { mockAppender.formatter } returns mockk()

        LoggerFactory.configure {
            addAppender(mockAppender)
            level = LogLevel.TRACE // Ensure messages aren't filtered by level
        }

        val logger = LoggerFactory.getLogger("ConfigTest")
        logger.info("Test message")

        // Need to wait for async dispatch in DefaultLogger
        Thread.sleep(100) // Adjust timing as needed, or use TestCoroutineDispatcher

        coVerify(timeout = 500) { mockAppender.append(any()) } // Use coVerify for suspend fun in dispatcher
    }

    @Test
    fun `shutdown stops appenders`() {
        val mockAppender = mockk<Appender>(relaxed = true)
        every { mockAppender.formatter } returns mockk()

        LoggerFactory.configure {
            addAppender(mockAppender)
        }

        LoggerFactory.shutdown()

        verify { mockAppender.stop() }
    }
}