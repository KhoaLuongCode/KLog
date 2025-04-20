package io.github.khoaluong.logging.internal.logger

import io.github.khoaluong.logging.api.Appender
import io.github.khoaluong.logging.api.Filter
import io.github.khoaluong.logging.api.LogLevel
import io.github.khoaluong.logging.api.Logger
import io.github.khoaluong.logging.internal.LogDispatcher
import io.github.khoaluong.logging.internal.appenders.ConsoleAppender
import io.github.khoaluong.logging.internal.filters.LevelFilter
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/**
 * Factory object for obtaining Logger instances.
 * This is the main entry point for users to get loggers.
 * It also provides basic configuration capabilities.
 */
object LoggerFactory {

    fun getLogger(
        kClass: KClass<*> = DefaultLogger::class,
        name: String? = null,
        logLevel: LogLevel = LogDispatcher.level,
        filters: List<Filter> = emptyList(),
        vararg appenders: Appender
    ): Logger? {
        try {
            val loggerConstructor = kClass.primaryConstructor
            val logger = loggerConstructor?.call(
                name ?: kClass.simpleName,
                LevelFilter(logLevel),
                filters,
                appenders
            ) as Logger
            LogDispatcher.registerLogger(logger)
            return logger
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getDefaultLogger(
        name: String = "DefaultLogger",
        logLevel: LogLevel = LogDispatcher.level,
        filters: List<Filter> = emptyList(),
        vararg appenders: Appender
    ): DefaultLogger {
        return getLogger(DefaultLogger::class, name, logLevel, filters, *appenders)!! as DefaultLogger
    }

    fun getConsoleLogger(
    name: String = "ConsoleLogger",
    logLevel: LogLevel = LogDispatcher.level,
    filters: List<Filter> = emptyList(),
    target: ConsoleAppender.ConsoleTarget = ConsoleAppender.ConsoleTarget.STDOUT,
    vararg appenders: Appender
    ): DefaultLogger {
        return getLogger(DefaultLogger::class, name, logLevel, filters,
            *arrayOf(*appenders , ConsoleAppender.createConsoleAppender(target=target)))!! as DefaultLogger
    }

}