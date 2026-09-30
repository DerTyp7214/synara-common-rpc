package dev.dertyp.serializers

import dev.dertyp.data.Change
import dev.dertyp.data.ChangeTopic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class ChangeTopicSerializerTest {

    @Serializable
    private data class RawChange(val topic: String)

    @Test
    fun knownTopicsRoundTripInJson() {
        ChangeTopic.entries.forEach { topic ->
            val json = AppJson.encodeToString(Change.serializer(), Change(topic))
            assertEquals("{\"topic\":\"${topic.name}\"}", json)
            assertEquals(Change(topic), AppJson.decodeFromString(Change.serializer(), json))
        }
    }

    @Test
    fun knownTopicsRoundTripInCbor() {
        ChangeTopic.entries.forEach { topic ->
            val bytes = AppCbor.encodeToByteArray(Change.serializer(), Change(topic))
            assertEquals(Change(topic), AppCbor.decodeFromByteArray(Change.serializer(), bytes))
            assertEquals(RawChange(topic.name), AppCbor.decodeFromByteArray(RawChange.serializer(), bytes))
        }
    }

    @Test
    fun unknownTopicDecodesToUnknownInJson() {
        val decoded = AppJson.decodeFromString(Change.serializer(), "{\"topic\":\"SOME_FUTURE_TOPIC\"}")
        assertEquals(ChangeTopic.UNKNOWN, decoded.topic)
    }

    @Test
    fun unknownTopicDecodesToUnknownInCbor() {
        val bytes = AppCbor.encodeToByteArray(RawChange.serializer(), RawChange("SOME_FUTURE_TOPIC"))
        val decoded = AppCbor.decodeFromByteArray(Change.serializer(), bytes)
        assertEquals(ChangeTopic.UNKNOWN, decoded.topic)
    }
}
