val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val assertj_version: String by project
val junit_version: String by project
val mockk_version: String by project
val kmongo_version: String by project
val testcontainers_version: String by project
val koin_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.0"
    jacoco // see https://youtrack.jetbrains.com/issue/KT-44757
}

group = "fr.tle"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.litote.kmongo:kmongo-serialization:$kmongo_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.assertj:assertj-core:$assertj_version")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
        }
    }
}