package dev.dertyp.serializers

import dev.dertyp.PlatformLocalDateTime
import dev.dertyp.formatISO
import dev.dertyp.toPlatformLocalDateTimeISO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateTimeSerializer : KSerializer<PlatformLocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformLocalDateTime) {
        encoder.encodeString(value.formatISO())
    }

    override fun deserialize(decoder: Decoder): PlatformLocalDateTime {
        return decoder.decodeString().toPlatformLocalDateTimeISO()
    }
}
