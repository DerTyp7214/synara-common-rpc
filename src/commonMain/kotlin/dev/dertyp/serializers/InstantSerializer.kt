@file:OptIn(kotlin.time.ExperimentalTime::class)

package dev.dertyp.serializers

import dev.dertyp.PlatformInstant
import dev.dertyp.formatISO
import dev.dertyp.toPlatformInstantISO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<PlatformInstant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformInstant) {
        encoder.encodeString(value.formatISO())
    }

    override fun deserialize(decoder: Decoder): PlatformInstant {
        return decoder.decodeString().toPlatformInstantISO()
    }
}
