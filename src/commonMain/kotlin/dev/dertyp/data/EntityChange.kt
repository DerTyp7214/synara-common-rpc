@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@ModelDoc("The kind of library entity a recorded change is about.")
enum class EntityType {
    @FieldDoc("An entity type this client does not know yet. Clients ignore it.")
    UNKNOWN,

    @FieldDoc("A song.")
    SONG,

    @FieldDoc("An album.")
    ALBUM,

    @FieldDoc("An artist.")
    ARTIST,

    @FieldDoc("A playlist a user owns.")
    USER_PLAYLIST,

    @FieldDoc("A global playlist, mirrored or imported from a platform.")
    PLAYLIST,

    @FieldDoc("A collection.")
    COLLECTION
}

object EntityTypeSerializer : KSerializer<EntityType> {
    override val descriptor: SerialDescriptor = EntityType.serializer().descriptor

    override fun serialize(encoder: Encoder, value: EntityType) {
        EntityType.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): EntityType {
        val name = decoder.decodeString()
        return EntityType.entries.firstOrNull { it.name == name } ?: EntityType.UNKNOWN
    }
}

@Serializable
@ModelDoc("The part of an entity that changed. Parts change independently, so a like says nothing about the entity's own fields.")
enum class EntityChangeAspect {
    @FieldDoc("A part this client does not know yet. Clients ignore it.")
    UNKNOWN,

    @FieldDoc("The entity's own fields a client can read, such as names, tags, credits, cover, lyrics, numbers and links.")
    DATA,

    @FieldDoc(
        "What the entity contains, such as the songs of an album or playlist, the items of a collection, or the albums and songs of an artist."
    )
    MEMBERS,

    @FieldDoc(
        "The like state of the calling user, meaning the like level of a song, the star of an album or following an artist. " +
            "It belongs to one user."
    )
    LIKE,

    @FieldDoc("The timecode tags of the calling user on a song. It belongs to one user.")
    TIMECODES
}

object EntityChangeAspectSerializer : KSerializer<EntityChangeAspect> {
    override val descriptor: SerialDescriptor = EntityChangeAspect.serializer().descriptor

    override fun serialize(encoder: Encoder, value: EntityChangeAspect) {
        EntityChangeAspect.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): EntityChangeAspect {
        val name = decoder.decodeString()
        return EntityChangeAspect.entries.firstOrNull { it.name == name } ?: EntityChangeAspect.UNKNOWN
    }
}

@Serializable
@ModelDoc("What happened to the part of an entity a recorded change is about.")
enum class EntityChangeKind {
    @FieldDoc("A kind this client does not know yet. Clients treat it as a reason to read the entity again.")
    UNKNOWN,

    @FieldDoc(
        "The entity was created. An entry keeps CREATED when the entity is updated afterwards and only its time moves, so a client that " +
            "already has the entity can receive CREATED again and reads the entity again."
    )
    CREATED,

    @FieldDoc("The part of the entity was updated.")
    UPDATED,

    @FieldDoc("The entity was deleted.")
    DELETED
}

object EntityChangeKindSerializer : KSerializer<EntityChangeKind> {
    override val descriptor: SerialDescriptor = EntityChangeKind.serializer().descriptor

    override fun serialize(encoder: Encoder, value: EntityChangeKind) {
        EntityChangeKind.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): EntityChangeKind {
        val name = decoder.decodeString()
        return EntityChangeKind.entries.firstOrNull { it.name == name } ?: EntityChangeKind.UNKNOWN
    }
}

@Serializable
@ModelDoc(
    "A recorded change of one part of one entity. An entry carries no data, it says that this part of this entity changed and has to be read again. " +
        "Only the latest change per entity and part is kept, so several changes of the same part arrive as one entry. " +
        "A deleted entity appears as a single entry with the aspect DATA and the kind DELETED."
)
data class EntityChange(
    @FieldDoc("The kind of entity that changed. Types a client does not know are decoded as UNKNOWN.")
    @Serializable(with = EntityTypeSerializer::class)
    val entityType: EntityType,
    @FieldDoc("The unique identifier of the entity that changed.")
    val entityId: PlatformUUID,
    @FieldDoc("The part of the entity that changed. Parts a client does not know are decoded as UNKNOWN.")
    @Serializable(with = EntityChangeAspectSerializer::class)
    val aspect: EntityChangeAspect,
    @FieldDoc("What happened to that part. Kinds a client does not know are decoded as UNKNOWN.")
    @Serializable(with = EntityChangeKindSerializer::class)
    val kind: EntityChangeKind,
    @FieldDoc("When the change was recorded (epoch milliseconds).")
    val changedAt: Long
)

@Serializable
@ModelDoc("The time frame a client needs around a pull of recorded changes. It tells which time to ask from next and how far back the recorded changes reach.")
data class EntityChangeWindow(
    @FieldDoc(
        "The time the client passes as since on its next pull (epoch milliseconds). It lies slightly in the past, so a change written while " +
            "the client was pulling is not missed. Entries can therefore repeat between two pulls."
    )
    val serverTime: Long,
    @FieldDoc(
        "The earliest time from which the recorded changes are complete (epoch milliseconds). A client whose since lies before it reads " +
            "everything again instead of pulling."
    )
    val availableSince: Long
)
