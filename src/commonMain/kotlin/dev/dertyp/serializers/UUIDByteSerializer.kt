package dev.dertyp.serializers

import dev.dertyp.PlatformUUID
import dev.dertyp.toByteArray
import dev.dertyp.toPlatformUUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UUIDByteSerializer : KSerializer<PlatformUUID> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlatformUUID")

    override fun serialize(encoder: Encoder, value: PlatformUUID) {
        encoder.encodeSerializableValue(ByteArraySerializer(), value.toByteArray())
    }

    override fun deserialize(decoder: Decoder): PlatformUUID {
        return try {
            val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
            bytes.toPlatformUUID()
        } catch (_: Exception) {
            decoder.decodeString().toPlatformUUID()
        }
    }
}

object UUIDByteListSerializer : KSerializer<List<PlatformUUID>> {
    private val delegateSerializer = ListSerializer(UUIDByteSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<PlatformUUID>) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<PlatformUUID> {
        return delegateSerializer.deserialize(decoder)
    }
}
