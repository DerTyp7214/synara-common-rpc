plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.symbol.processing.api)
    implementation(project(":common-rpc"))

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--sun-misc-unsafe-memory-access=allow", "-Xshare:off")
}
