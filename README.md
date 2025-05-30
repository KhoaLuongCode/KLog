## io.github.khoaluong.logging

A modern, Kotlin-idiomatic logging library designed for simplicity, flexibility, and performance in Kotlin applications.

## Introduction

`io.github.khoaluong.logging` is a lightweight logging framework tailored for Kotlin developers. It provides a clean, extensible API that integrates seamlessly with Kotlin’s language features, including coroutines and serialization. Whether you're building a small application or a large-scale system, this library offers a robust solution for logging with minimal boilerplate and maximum customizability.

## Why Use This Library?

- **Kotlin-First**: Built with Kotlin’s strengths in mind, offering a fluent, type-safe API using lambdas, extensions, and data classes.  
- **Extensible**: Easily customize log output, formatting, and filtering to suit your application’s needs.  
- **Coroutine-Aware**: Native support for context propagation in asynchronous Kotlin applications.  
- **Structured Logging**: Supports machine-readable formats like JSON for advanced log analysis.  
- **Performant**: Features lazy message evaluation and asynchronous logging for minimal performance impact.

## Key Features

- **Standard Log Levels**: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` for flexible logging granularity.  
- **Pluggable Components**:
  - **Appenders**: Output logs to console, files, or custom destinations.  
  - **Formatters**: Format logs as plain text, JSON, or custom formats.  
  - **Filters**: Fine-tune which logs are processed based on level, content, or context.  
- **Asynchronous Logging**: Non-blocking log processing with `AsyncAppender` to enhance application responsiveness.  
- **Coroutine Context Propagation**: Maintain contextual data (e.g., user IDs, request IDs) across coroutine scopes.  
- **Structured Logging**: Serialize log events for integration with log management systems.  
- **Thread-Safe**: Safe for use in concurrent environments with proper resource management.

## Getting Started

### Installation

Add the library to your project via Gradle. Replace `LATEST_VERSION` with the current release version:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.khoaluong:logging-api:LATEST_VERSION")
    // Optional: for JSON formatting
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10" // Required for JSON formatting
}
```

## Basic Usage

### Configure the Logging System

```
import io.github.khoaluong.logging.api.*

fun main() {
    LoggerFactory.configure {
        minimumLevel = LogLevel.INFO
        addAppender(ConsoleAppender(SimpleFormatter()))
    }
}
```
### Obtain a logger

```
val logger = LoggerFactory.getLogger("MyApp")
```

### Log Messages

```
logger.info { "Application started!" }
logger.error("An error occurred", exception)
```

## Advanced Configuration

Customize logging behavior by adding appenders, formatters, and filters:

```
LoggerFactory.configure {
    minimumLevel = LogLevel.DEBUG
    addAppender(AsyncAppender(ConsoleAppender(JsonFormatter())))
    addFilter(LevelFilter(LogLevel.WARN)) // Only process WARN and above
}
```

For coroutine support, use LoggingContextElement to propagate context:

```
coroutineScope {
    launch(LoggingContextElement("requestId" to "12345")) {
        logger.info { "Processing request" }
    }
}
```

## Extensibility

The library is designed for customization:

- Custom Appenders: Implement the Appender interface to send logs to databases, network services, or other destinations.
- Custom Formatters: Implement the Formatter interface for unique log output styles (e.g., XML, CSV).
- Custom Filters: Implement the Filter interface to control log processing based on custom criteria.

Example custom appender:
```
class CustomAppender : Appender {
    override fun append(event: LogEvent) {
        // Send to external service
    }
    override fun stop() {
        // Clean up resources
    }
}
```

## Performance Considerations

- Lazy Evaluation: Log messages are evaluated only if the log level is enabled, reducing overhead.
- Asynchronous Logging: Use AsyncAppender to offload I/O operations to a background thread.
- Resource Management: Properly shut down the logging system with LoggerFactory.shutdown() to release resources.

Happy logging!




