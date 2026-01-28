package dev.dertyp.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.ByteBuffer
import java.util.UUID

object UUIDByteSerializer: KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        val bytes = ByteBuffer.allocate(16)
            .putLong(value.mostSignificantBits)
            .putLong(value.leastSignificantBits)
            .array()
        encoder.encodeSerializableValue(ByteArraySerializer(), bytes)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
        require(bytes.size == 16) { "UUID must be exactly 16 bytes" }
        val buffer = ByteBuffer.wrap(bytes)
        return UUID(buffer.long, buffer.long)
    }
}

object UUIDByteListSerializer : KSerializer<List<UUID>> {
    private val delegateSerializer = ListSerializer(UUIDByteSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<UUID>) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<UUID> {
        return delegateSerializer.deserialize(decoder)
    }
}