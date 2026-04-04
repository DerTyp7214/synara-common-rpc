@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.rpc

import dev.dertyp.getPlatformName
import dev.dertyp.serializers.AppCbor
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import kotlinx.rpc.krpc.ktor.client.*
import kotlinx.rpc.krpc.serialization.cbor.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.reflect.KClass

fun createRpcHttpClient(appVersion: String): HttpClient {
    // Automatically initialize the registry from generated code
    initializeServiceRegistry()
    
    return HttpClient {
        install(UserAgent) {
            agent = "Synara/$appVersion (${getPlatformName()})"
        }
        install(WebSockets)
        install(Krpc) {
            serialization {
                cbor(AppCbor)
            }
        }
    }
}

/**
 * A dynamic registry of service interfaces to their KClass objects.
 * Populated automatically by the KSP compiler via initializeServiceRegistry().
 */
private val serviceClassRegistry = mutableMapOf<String, KClass<*>>()

fun registerServiceClass(name: String, clazz: KClass<*>) {
    serviceClassRegistry[name] = clazz
}

fun getServiceClass(name: String): KClass<*>? {
    return serviceClassRegistry[name]
}

// Placeholder for the generated initialization function
expect fun initializeServiceRegistry()
