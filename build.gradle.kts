import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.rpc)
}

val hasAndroidEnv = System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        File(projectDir.parentFile, "local.properties").exists()

if (hasAndroidEnv) {
    pluginManager.apply("com.android.kotlin.multiplatform.library")
}

group = "de.dertyp7214"
version = "1.0.0"

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()

    if (hasAndroidEnv) {
        extensions.findByName("androidLibrary")?.let { extension ->
            val namespaceMethod = extension.javaClass.getMethod("setNamespace", String::class.java)
            val compileSdkMethod =
                extension.javaClass.getMethod("setCompileSdk", Int::class.javaObjectType)
            val minSdkMethod = extension.javaClass.getMethod("setMinSdk", Int::class.javaObjectType)

            namespaceMethod.invoke(extension, "de.dertyp7214.common_rpc")
            compileSdkMethod.invoke(extension, 36)
            minSdkMethod.invoke(extension, 26)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    mingwX64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        commonMain.dependencies {
            api(libs.kotlinx.rpc.core)
            api(libs.kotlinx.serialization.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.serialization.cbor)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
