package dev.dertyp.serializers

import dev.dertyp.PlatformDate
import dev.dertyp.platformDateFromEpochMilliseconds
import dev.dertyp.toEpochMilliseconds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateSerializer : KSerializer<PlatformDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: PlatformDate) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): PlatformDate {
        return platformDateFromEpochMilliseconds(decoder.decodeLong())
    }
}
