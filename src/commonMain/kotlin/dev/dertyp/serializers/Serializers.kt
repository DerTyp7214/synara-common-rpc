@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val BaseSerializersModule = SerializersModule {
    contextual(DateSerializer)
    contextual(LocalDateSerializer)
    contextual(LocalDateTimeSerializer)
    contextual(OffsetDateTimeSerializer)
    contextual(DurationSerializer)
    contextual(InstantSerializer)
}

val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        include(BaseSerializersModule)
        contextual(UUIDSerializer)
    }
}

val AppCbor = Cbor {
    alwaysUseByteString = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        include(BaseSerializersModule)
        contextual(UUIDByteSerializer)
    }
}
