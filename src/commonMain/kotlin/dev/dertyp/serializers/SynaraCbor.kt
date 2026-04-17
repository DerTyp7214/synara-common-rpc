@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.serializers

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.Artist
import dev.dertyp.data.Genre
import dev.dertyp.data.Image
import kotlinx.rpc.krpc.serialization.KrpcSerialFormat
import kotlinx.rpc.krpc.serialization.KrpcSerialFormatBuilder
import kotlinx.rpc.krpc.serialization.KrpcSerialFormatConfiguration
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.cbor.CborBuilder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlin.reflect.KClass

val SynaraDeduplicatedTypes = listOf(
    Artist::class to Artist.serializer(),
    Album::class to Album.serializer(),
    Genre::class to Genre.serializer(),
    Image::class to Image.serializer(),
)

class SynaraPool {
    internal val items = mutableMapOf<KClass<*>, MutableMap<PlatformUUID, Any>>()
    fun <T : Any> getPool(kClass: KClass<T>): MutableMap<PlatformUUID, T> {
        @Suppress("UNCHECKED_CAST")
        return items.getOrPut(kClass) { mutableMapOf() } as MutableMap<PlatformUUID, T>
    }
}

expect object SynaraNegotiation {
    var isEnabled: Boolean
}

private object SynaraCborFormat : KrpcSerialFormat<SynaraCbor, CborBuilder> {
    override fun withBuilder(from: SynaraCbor?, builderConsumer: CborBuilder.() -> Unit): SynaraCbor {
        return SynaraCbor(Cbor(from?.cbor ?: Cbor, builderConsumer))
    }
    override fun CborBuilder.applySerializersModule(serializersModule: SerializersModule) {
        this.serializersModule = this.serializersModule.overwriteWith(serializersModule)
    }
}

fun KrpcSerialFormatConfiguration.synaraCbor(cbor: Cbor = Cbor.Default) {
    register(KrpcSerialFormatBuilder.Binary(SynaraCborFormat, SynaraCbor(cbor)) {})
}

class SynaraCbor(val cbor: Cbor) : BinaryFormat {
    override val serializersModule: SerializersModule = cbor.serializersModule
    private val classToSerializer = SynaraDeduplicatedTypes.toMap()
    private val nameToSerializer = SynaraDeduplicatedTypes.associate { it.second.descriptor.serialName to it.second }
    private val nameToClass = SynaraDeduplicatedTypes.associate { it.second.descriptor.serialName to it.first }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        if (!SynaraNegotiation.isEnabled) {
            return cbor.encodeToByteArray(serializer, value)
        }
        
        val pool = SynaraPool()
        val collector = CollectorEncoder(pool, classToSerializer, serializersModule)
        collector.encodeSerializableValue(serializer, value)
        
        val packModule = SerializersModule {
            SynaraDeduplicatedTypes.forEach { (kClass, typeSer) ->
                @Suppress("UNCHECKED_CAST")
                contextual(kClass as KClass<Any>, ReferenceSerializer(pool, kClass, typeSer as KSerializer<Any>))
            }
        }
        
        val packCbor = Cbor(cbor) { serializersModule = cbor.serializersModule.overwriteWith(packModule) }

        val encodedPool = pool.items.mapNotNull { (kClass, items) ->
            val ser = classToSerializer[kClass] ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            val bytes = cbor.encodeToByteArray(ListSerializer(ser as KSerializer<Any>), items.values.toList())
            ser.descriptor.serialName to bytes
        }.toMap()
        
        val envelope = SynaraEnvelope(encodedPool, packCbor.encodeToByteArray(serializer, value))
        return cbor.encodeToByteArray(SynaraEnvelope.serializer(), envelope)
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        if (!SynaraNegotiation.isEnabled) {
            return cbor.decodeFromByteArray(deserializer, bytes)
        }

        val pool = SynaraPool()
        val envelope = try {
            cbor.decodeFromByteArray(SynaraEnvelope.serializer(), bytes)
        } catch (_: Exception) {
            return cbor.decodeFromByteArray(deserializer, bytes)
        }
        
        envelope.pool.forEach { (name, poolBytes) ->
            val ser = nameToSerializer[name] ?: return@forEach
            @Suppress("UNCHECKED_CAST")
            val items = cbor.decodeFromByteArray(ListSerializer(ser as KSerializer<Any>), poolBytes)
            val kClass = nameToClass[name]!!
            @Suppress("UNCHECKED_CAST")
            val typePool = pool.getPool(kClass as KClass<Any>)
            items.forEach { typePool[getIdOf(it, ser)] = it }
        }
        
        val packModule = SerializersModule {
            SynaraDeduplicatedTypes.forEach { (kClass, typeSer) ->
                @Suppress("UNCHECKED_CAST")
                contextual(kClass as KClass<Any>, ReferenceSerializer(pool, kClass, typeSer as KSerializer<Any>))
            }
        }
        
        val packCbor = Cbor(cbor) { serializersModule = cbor.serializersModule.overwriteWith(packModule) }
        return packCbor.decodeFromByteArray(deserializer, envelope.data)
    }
}

@Serializable
private class SynaraEnvelope(val pool: Map<String, ByteArray>, val data: ByteArray)

private class CollectorEncoder(val pool: SynaraPool, val classToSerializer: Map<KClass<*>, KSerializer<*>>, override val serializersModule: SerializersModule) : Encoder {
    override fun beginStructure(descriptor: SerialDescriptor) = object : CompositeEncoder {
        override val serializersModule = this@CollectorEncoder.serializersModule
        override fun endStructure(descriptor: SerialDescriptor) {}
        override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {}
        override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {}
        override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {}
        override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {}
        override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {}
        override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {}
        override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {}
        override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {}
        override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {}
        override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = this@CollectorEncoder
        override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) { if (value != null) encodeSerializableValue(serializer, value) }
        override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) { if (value != null) encodeSerializableValue(serializer, value) }
    }
    override fun encodeBoolean(value: Boolean) {}
    override fun encodeByte(value: Byte) {}
    override fun encodeChar(value: Char) {}
    override fun encodeDouble(value: Double) {}
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {}
    override fun encodeFloat(value: Float) {}
    override fun encodeInt(value: Int) {}
    override fun encodeLong(value: Long) {}
    override fun encodeNotNullMark() {}
    override fun encodeNull() {}
    override fun encodeShort(value: Short) {}
    override fun encodeString(value: String) {}
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (value == null) return
        val kClass = value::class
        val ser = classToSerializer[kClass]
        if (ser != null) {
            val id = getIdOf(value, ser)
            @Suppress("UNCHECKED_CAST")
            val p = pool.getPool(kClass as KClass<Any>)
            if (!p.containsKey(id)) {
                p[id] = value
                @Suppress("UNCHECKED_CAST")
                (ser as KSerializer<Any>).serialize(this, value)
            }
        } else serializer.serialize(this, value)
    }
}

private class ReferenceSerializer<T : Any>(val pool: SynaraPool, val kClass: KClass<T>, val baseSerializer: KSerializer<T>) : KSerializer<T> {
    override val descriptor = contextualPlatformUUIDSerializer().descriptor
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeSerializableValue(contextualPlatformUUIDSerializer(), getIdOf(value, baseSerializer))
    override fun deserialize(decoder: Decoder): T {
        val id = decoder.decodeSerializableValue(contextualPlatformUUIDSerializer())
        return pool.getPool(kClass)[id] ?: throw SerializationException("Object $id of ${kClass.simpleName} not in pool")
    }
}

private fun getIdOf(value: Any, serializer: KSerializer<*>): PlatformUUID {
    val desc = serializer.descriptor
    val idx = (0 until desc.elementsCount).first { desc.getElementName(it) == "id" }
    var id: PlatformUUID? = null
    val stealer = object : Encoder by DummyEncoder {
        override fun beginStructure(descriptor: SerialDescriptor) = object : CompositeEncoder by DummyComposite {
            override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) { if (index == idx) id = value as PlatformUUID }
            override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) { if (index == idx) id = value as PlatformUUID }
        }
    }
    @Suppress("UNCHECKED_CAST") (serializer as KSerializer<Any>).serialize(stealer, value)
    return id!!
}

private fun contextualPlatformUUIDSerializer(): KSerializer<PlatformUUID> {
    @Suppress("UNCHECKED_CAST")
    return UUIDByteSerializer as KSerializer<PlatformUUID>
}

private object DummyEncoder : Encoder {
    override val serializersModule = EmptySerializersModule()
    override fun beginStructure(descriptor: SerialDescriptor) = DummyComposite
    override fun encodeBoolean(value: Boolean) {}
    override fun encodeByte(value: Byte) {}
    override fun encodeChar(value: Char) {}
    override fun encodeDouble(value: Double) {}
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {}
    override fun encodeFloat(value: Float) {}
    override fun encodeInt(value: Int) {}
    override fun encodeLong(value: Long) {}
    override fun encodeNotNullMark() {}
    override fun encodeNull() {}
    override fun encodeShort(value: Short) {}
    override fun encodeString(value: String) {}
    override fun encodeInline(descriptor: SerialDescriptor) = this
}

private object DummyComposite : CompositeEncoder {
    override val serializersModule = EmptySerializersModule()
    override fun endStructure(descriptor: SerialDescriptor) {}
    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {}
    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {}
    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {}
    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {}
    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {}
    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {}
    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {}
    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {}
    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {}
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int) = DummyEncoder
    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {}
    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {}
}
