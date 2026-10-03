package dev.dertyp.serializers

import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class TitleTagKindSerializerTest {

    @Serializable
    private data class RawTitleTag(val kind: String, val label: String)

    @Test
    fun knownKindsRoundTripInJson() {
        TitleTagKind.entries.forEach { kind ->
            val json = AppJson.encodeToString(TitleTag.serializer(), TitleTag(kind, "Label"))
            assertEquals("{\"kind\":\"${kind.name}\",\"label\":\"Label\"}", json)
            assertEquals(TitleTag(kind, "Label"), AppJson.decodeFromString(TitleTag.serializer(), json))
        }
    }

    @Test
    fun knownKindsRoundTripInCbor() {
        TitleTagKind.entries.forEach { kind ->
            val bytes = AppCbor.encodeToByteArray(TitleTag.serializer(), TitleTag(kind, "Label"))
            assertEquals(TitleTag(kind, "Label"), AppCbor.decodeFromByteArray(TitleTag.serializer(), bytes))
            assertEquals(RawTitleTag(kind.name, "Label"), AppCbor.decodeFromByteArray(RawTitleTag.serializer(), bytes))
        }
    }

    @Test
    fun unknownKindDecodesToUnknownInJson() {
        val decoded =
            AppJson.decodeFromString(TitleTag.serializer(), "{\"kind\":\"SOME_FUTURE_KIND\",\"label\":\"Label\"}")
        assertEquals(TitleTag(TitleTagKind.UNKNOWN, "Label"), decoded)
    }

    @Test
    fun unknownKindDecodesToUnknownInCbor() {
        val bytes = AppCbor.encodeToByteArray(RawTitleTag.serializer(), RawTitleTag("SOME_FUTURE_KIND", "Label"))
        val decoded = AppCbor.decodeFromByteArray(TitleTag.serializer(), bytes)
        assertEquals(TitleTag(TitleTagKind.UNKNOWN, "Label"), decoded)
    }
}
