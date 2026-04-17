@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.serializers

import dev.dertyp.data.Album
import dev.dertyp.data.Artist
import dev.dertyp.data.Genre
import dev.dertyp.data.Image
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
    contextual(Artist.serializer())
    contextual(Album.serializer())
    contextual(Genre.serializer())
    contextual(Image.serializer())
}

const val SynaraPackHeader = "X-Synara-Pack"

val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    useArrayPolymorphism = false
    prettyPrint = false
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
