plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(kotlin("stdlib"))
}
