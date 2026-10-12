plugins {
    alias(libs.plugins.kotlin.multiplatform)
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

            namespaceMethod.invoke(extension, "de.dertyp7214.common_rpc.rest_client")
            compileSdkMethod.invoke(extension, 36)
            minSdkMethod.invoke(extension, 26)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64("macosarm")
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    mingwX64("windows")
    linuxX64("linux")

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.time.ExperimentalTime")
                if (name != "commonMain" && !name.startsWith("jvm") && !name.startsWith("commonTest")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }

        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
            dependencies {
                api(project(":common-rpc"))
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-opt-in=kotlin.experimental.ExperimentalObjCName")
    }
}

val rpcInterfacesFile = project(":common-rpc").layout.buildDirectory
    .file("generated/ksp/metadata/commonMain/resources/dev/dertyp/rpc/rpc-interfaces.txt")

ksp {
    arg("rest.mode", "client")
    arg("rest.interfacesFile", rpcInterfacesFile.get().asFile.absolutePath)
    arg("rest.client.package", "dev.dertyp.rpc.rest.client")
}

dependencies {
    add("kspCommonMainMetadata", project(":common-rpc:rest-compiler"))
}

tasks.matching { it.name == "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn(":common-rpc:kspCommonMainKotlinMetadata")
    inputs.file(rpcInterfacesFile)
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
