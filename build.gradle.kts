import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.rpc)
    alias(libs.plugins.ksp)
}


val includeAndroid = project.findProperty("synara.includeAndroid")?.toString()?.toBoolean()
    ?: rootProject.file("local.properties").exists()


val hasAndroidEnv = includeAndroid && (
        System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        rootProject.file("local.properties").exists()
)

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

    val nativeTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64("macosx64"),
        macosArm64("macosarm"),
        mingwX64("windows"),
        linuxX64("linux")
    )

    nativeTargets.forEach { target ->
        target.binaries {
            staticLib {
                baseName = "common_rpc"
            }
        }
    }

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
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        commonMain {
            dependencies {
                api(libs.kotlinx.rpc.core)
                api(libs.kotlinx.rpc.krpc.ktor.client)
                api(libs.ktor.client.core)
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.serialization.cbor)
            }
        }

        val nativeMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/resources"))
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}


dependencies {
    add("kspCommonMainMetadata", project(":common-rpc:compiler"))
}
