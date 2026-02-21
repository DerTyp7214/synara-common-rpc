package dev.dertyp.serializers

import dev.dertyp.PlatformLocalDate
import dev.dertyp.formatISO
import dev.dertyp.toPlatformLocalDateISO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<PlatformLocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformLocalDate) {
        encoder.encodeString(value.formatISO())
    }

    override fun deserialize(decoder: Decoder): PlatformLocalDate {
        return decoder.decodeString().toPlatformLocalDateISO()
    }
}
