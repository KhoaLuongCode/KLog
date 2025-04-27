import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.maven.MavenPublication

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
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11)) // Or 17, 21 etc. - choose your desired minimum JVM
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])


            groupId = project.group as String
            artifactId = rootProject.name
            version = project.version as String

        }
    }
    repositories {
        mavenLocal()
    }
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("dokka"))
}