package io.github.khoaluong.logging.extensions

import io.github.khoaluong.logging.api.Logger
import io.github.khoaluong.logging.api.LoggerFactory
import kotlin.reflect.KClass

/**
 * Provides convenient extension functions for obtaining logger instances.
 */

/**
 * Gets a logger named after the receiver's class.
 * Example: `val logger = MyClass().getLogger()`
 */
fun Any.getLogger(): Logger = LoggerFactory.getLogger(this::class.java)

/**
 * Gets a logger named after the KClass.
 * Example: `val logger = MyClass::class.getLogger()`
 */
fun KClass<*>.getLogger(): Logger = LoggerFactory.getLogger(this.qualifiedName ?: this.java.name)

/**
 * Gets a logger named after the Java Class.
 * Example: `val logger = MyClass::class.java.getLogger()`
 */
fun Class<*>.getLogger(): Logger = LoggerFactory.getLogger(this.name)

/**
 * Utility to get logger inside companion objects more easily.
 * Example:
 * ```kotlin
 * class MyService {
 *     companion object : WithLogger {} // Inherit marker interface
 *
 *     fun doSomething() {
 *         logger.info("Doing something") // Access logger directly
 *     }
 * }
 * ```
 */
interface WithLogger {
    // Default implementation gets logger named after the *enclosing* class of the companion object
    val logger: Logger
        get() = LoggerFactory.getLogger(
            javaClass.enclosingClass?.name ?: javaClass.name
        )
}

/**
 * Gets a logger directly, typically used at the top level of a file.
 * Example: `private val logger = getLogger()` at the top of `MyUtil.kt`
 */
inline fun getLogger(): Logger = LoggerFactory.getLogger() // Uses stack trace inspection