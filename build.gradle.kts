plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.rpc)
}

group = "de.dertyp7214"
version = "1.0.0"

dependencies {
    api(libs.kotlinx.rpc.core)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.cbor)
}

kotlin {
    jvmToolchain(17)
}