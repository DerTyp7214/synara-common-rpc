package dev.dertyp.serializers

import dev.dertyp.PlatformUUID
import dev.dertyp.toPlatformUUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UUIDSerializer : KSerializer<PlatformUUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformUUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): PlatformUUID {
        return decoder.decodeString().toPlatformUUID()
    }
}

object UUIDListSerializer : KSerializer<List<PlatformUUID>> {
    private val delegateSerializer = ListSerializer(UUIDSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<PlatformUUID>) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<PlatformUUID> {
        return delegateSerializer.deserialize(decoder)
    }
}