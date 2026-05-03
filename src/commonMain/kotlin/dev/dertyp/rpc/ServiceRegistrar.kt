package dev.dertyp.rpc

import kotlinx.rpc.annotations.Rpc
import kotlin.reflect.KClass

interface ServiceRegistrar {
    fun <@Rpc T : Any> register(serviceKClass: KClass<T>, serviceFactory: () -> T)
}
