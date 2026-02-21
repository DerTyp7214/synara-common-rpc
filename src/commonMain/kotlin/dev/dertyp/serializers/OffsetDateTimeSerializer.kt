package dev.dertyp.serializers

import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.formatISO
import dev.dertyp.toPlatformOffsetDateTimeISO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object OffsetDateTimeSerializer : KSerializer<PlatformOffsetDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformOffsetDateTime) {
        encoder.encodeString(value.formatISO())
    }

    override fun deserialize(decoder: Decoder): PlatformOffsetDateTime {
        return decoder.decodeString().toPlatformOffsetDateTimeISO()
    }
}
