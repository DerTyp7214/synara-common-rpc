package dev.dertyp.serializers

import dev.dertyp.data.EntityChange
import dev.dertyp.data.EntityChangeAspect
import dev.dertyp.data.EntityChangeKind
import dev.dertyp.data.EntityChangeWindow
import dev.dertyp.data.EntityType
import dev.dertyp.platformUUIDFromString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class EntityChangeSerializerTest {

    @Serializable
    private data class RawEntityChange(
        val entityType: String,
        val entityId: String,
        val aspect: String,
        val kind: String,
        val changedAt: Long
    )

    private val entityId = platformUUIDFromString("00000000-0000-0000-0000-0000000000c1")

    private fun change(
        entityType: EntityType = EntityType.SONG,
        aspect: EntityChangeAspect = EntityChangeAspect.DATA,
        kind: EntityChangeKind = EntityChangeKind.UPDATED
    ) = EntityChange(entityType, entityId, aspect, kind, CHANGED_AT)

    private fun raw(
        entityType: String = EntityType.SONG.name,
        aspect: String = EntityChangeAspect.DATA.name,
        kind: String = EntityChangeKind.UPDATED.name
    ) = RawEntityChange(entityType, ENTITY_ID, aspect, kind, CHANGED_AT)

    private fun jsonField(change: EntityChange, field: String): String =
        AppJson.parseToJsonElement(AppJson.encodeToString(EntityChange.serializer(), change))
            .jsonObject.getValue(field).jsonPrimitive.content

    private fun decodeJson(raw: RawEntityChange): EntityChange =
        AppJson.decodeFromString(EntityChange.serializer(), AppJson.encodeToString(RawEntityChange.serializer(), raw))

    private fun decodeCbor(raw: RawEntityChange): EntityChange =
        AppCbor.decodeFromByteArray(EntityChange.serializer(), AppCbor.encodeToByteArray(RawEntityChange.serializer(), raw))

    private fun roundTripJson(change: EntityChange): EntityChange =
        AppJson.decodeFromString(EntityChange.serializer(), AppJson.encodeToString(EntityChange.serializer(), change))

    private fun roundTripCbor(change: EntityChange): EntityChange =
        AppCbor.decodeFromByteArray(EntityChange.serializer(), AppCbor.encodeToByteArray(EntityChange.serializer(), change))

    @Test
    fun knownEntityTypesRoundTripInJson() {
        EntityType.entries.forEach { type ->
            assertEquals(type.name, jsonField(change(entityType = type), "entityType"))
            assertEquals(change(entityType = type), roundTripJson(change(entityType = type)))
        }
    }

    @Test
    fun knownEntityTypesRoundTripInCbor() {
        EntityType.entries.forEach { type ->
            assertEquals(change(entityType = type), roundTripCbor(change(entityType = type)))
            assertEquals(change(entityType = type), decodeCbor(raw(entityType = type.name)))
        }
    }

    @Test
    fun unknownEntityTypeDecodesToUnknownInJson() {
        assertEquals(EntityType.UNKNOWN, decodeJson(raw(entityType = "SOME_FUTURE_TYPE")).entityType)
    }

    @Test
    fun unknownEntityTypeDecodesToUnknownInCbor() {
        assertEquals(EntityType.UNKNOWN, decodeCbor(raw(entityType = "SOME_FUTURE_TYPE")).entityType)
    }

    @Test
    fun knownAspectsRoundTripInJson() {
        EntityChangeAspect.entries.forEach { aspect ->
            assertEquals(aspect.name, jsonField(change(aspect = aspect), "aspect"))
            assertEquals(change(aspect = aspect), roundTripJson(change(aspect = aspect)))
        }
    }

    @Test
    fun knownAspectsRoundTripInCbor() {
        EntityChangeAspect.entries.forEach { aspect ->
            assertEquals(change(aspect = aspect), roundTripCbor(change(aspect = aspect)))
            assertEquals(change(aspect = aspect), decodeCbor(raw(aspect = aspect.name)))
        }
    }

    @Test
    fun unknownAspectDecodesToUnknownInJson() {
        assertEquals(EntityChangeAspect.UNKNOWN, decodeJson(raw(aspect = "SOME_FUTURE_ASPECT")).aspect)
    }

    @Test
    fun unknownAspectDecodesToUnknownInCbor() {
        assertEquals(EntityChangeAspect.UNKNOWN, decodeCbor(raw(aspect = "SOME_FUTURE_ASPECT")).aspect)
    }

    @Test
    fun knownKindsRoundTripInJson() {
        EntityChangeKind.entries.forEach { kind ->
            assertEquals(kind.name, jsonField(change(kind = kind), "kind"))
            assertEquals(change(kind = kind), roundTripJson(change(kind = kind)))
        }
    }

    @Test
    fun knownKindsRoundTripInCbor() {
        EntityChangeKind.entries.forEach { kind ->
            assertEquals(change(kind = kind), roundTripCbor(change(kind = kind)))
            assertEquals(change(kind = kind), decodeCbor(raw(kind = kind.name)))
        }
    }

    @Test
    fun unknownKindDecodesToUnknownInJson() {
        assertEquals(EntityChangeKind.UNKNOWN, decodeJson(raw(kind = "SOME_FUTURE_KIND")).kind)
    }

    @Test
    fun unknownKindDecodesToUnknownInCbor() {
        assertEquals(EntityChangeKind.UNKNOWN, decodeCbor(raw(kind = "SOME_FUTURE_KIND")).kind)
    }

    @Test
    fun entityChangeRoundTripsInJson() {
        val change = EntityChange(EntityType.ALBUM, entityId, EntityChangeAspect.MEMBERS, EntityChangeKind.DELETED, CHANGED_AT)

        val json = AppJson.encodeToString(EntityChange.serializer(), change)

        assertEquals(
            "{\"entityType\":\"ALBUM\",\"entityId\":\"$ENTITY_ID\",\"aspect\":\"MEMBERS\",\"kind\":\"DELETED\",\"changedAt\":$CHANGED_AT}",
            json
        )
        assertEquals(change, AppJson.decodeFromString(EntityChange.serializer(), json))
    }

    @Test
    fun entityChangeRoundTripsInCbor() {
        val change = EntityChange(EntityType.ALBUM, entityId, EntityChangeAspect.MEMBERS, EntityChangeKind.DELETED, CHANGED_AT)

        val bytes = AppCbor.encodeToByteArray(EntityChange.serializer(), change)

        assertEquals(change, AppCbor.decodeFromByteArray(EntityChange.serializer(), bytes))
    }

    @Test
    fun entityChangeWindowRoundTripsInJson() {
        val window = EntityChangeWindow(serverTime = CHANGED_AT, availableSince = CHANGED_AT - 2_592_000_000)

        val json = AppJson.encodeToString(EntityChangeWindow.serializer(), window)

        assertEquals("{\"serverTime\":$CHANGED_AT,\"availableSince\":${CHANGED_AT - 2_592_000_000}}", json)
        assertEquals(window, AppJson.decodeFromString(EntityChangeWindow.serializer(), json))
    }

    @Test
    fun entityChangeWindowRoundTripsInCbor() {
        val window = EntityChangeWindow(serverTime = CHANGED_AT, availableSince = CHANGED_AT - 2_592_000_000)

        val bytes = AppCbor.encodeToByteArray(EntityChangeWindow.serializer(), window)

        assertEquals(window, AppCbor.decodeFromByteArray(EntityChangeWindow.serializer(), bytes))
    }

    private companion object {
        const val ENTITY_ID = "00000000-0000-0000-0000-0000000000c1"
        const val CHANGED_AT = 1_790_000_000_000
    }
}
