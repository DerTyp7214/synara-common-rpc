plugins {
    kotlin("jvm") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    id("org.jetbrains.kotlinx.rpc.plugin") version "0.10.1"
}

group = "de.dertyp7214"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-rpc-core:0.10.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

kotlin {
    jvmToolchain(17)
}