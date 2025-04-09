import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "io.github.khoaluong.logging"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
}

// Configure Java Toolchain - THIS IS THE MAIN FIX
java {
    // Set the JDK version for compilation and running tests
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11)) // Or 17, 21 etc. - choose your desired minimum JVM
    }
    // Keep these inside the java block
    withSourcesJar()
    withJavadocJar()
}

// REMOVE or COMMENT OUT this block - The toolchain handles this now
/*
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11" // No longer needed when using toolchain
}
*/

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Optional: Basic configuration for publishing (if needed later)
/*
publishing {
   // ... your publishing config ...
}
*/

// Configure Dokka for documentation
tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("dokka"))
}