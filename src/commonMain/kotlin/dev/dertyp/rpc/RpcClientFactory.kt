@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.rpc

import dev.dertyp.getPlatformName
import dev.dertyp.serializers.AppCbor
import dev.dertyp.serializers.SynaraPackHeader
import dev.dertyp.serializers.synaraCbor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.reflect.KClass

fun createRpcHttpClient(appVersion: String): HttpClient {
    initializeServiceRegistry()
    
    return HttpClient {
        install(UserAgent) {
            agent = "Synara/$appVersion (${getPlatformName()})"
        }
        install(DefaultRequest) {
            header(SynaraPackHeader, "true")
        }
        install(WebSockets)
        install(Krpc) {
            serialization {
                synaraCbor(AppCbor)
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

expect fun initializeServiceRegistry()
