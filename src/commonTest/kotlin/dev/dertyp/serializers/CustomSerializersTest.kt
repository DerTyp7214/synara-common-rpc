package dev.dertyp.serializers

import dev.dertyp.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
class CustomSerializersTest {

    @Test
    fun testUUIDSerializer() {
        val uuidString = "550e8400-e29b-41d4-a716-446655440000"
        val uuid = platformUUIDFromString(uuidString)
        val json = Json.encodeToString(UUIDSerializer, uuid)
        assertEquals("\"$uuidString\"", json)
        assertEquals(uuid, Json.decodeFromString(UUIDSerializer, json))
    }

    @Test
    fun testUUIDByteSerializer() {
        val uuidString = "550e8400-e29b-41d4-a716-446655440000"
        val uuid = platformUUIDFromString(uuidString)

        val cborBytes = Cbor.encodeToByteArray(UUIDByteSerializer, uuid)
        val decoded = Cbor.decodeFromByteArray(UUIDByteSerializer, cborBytes)
        
        assertEquals(uuid, decoded)
        assertContentEquals(uuid.toByteArray(), decoded.toByteArray())
    }

    @Test
    fun testDateSerializer() {
        val ms = 1672531200000L // 2023-01-01T00:00:00Z
        val date = platformDateFromEpochMilliseconds(ms)
        val json = Json.encodeToString(DateSerializer, date)
        assertEquals(ms.toString(), json)
        assertEquals(ms, Json.decodeFromString(DateSerializer, json).toEpochMilliseconds())
    }

    @Test
    fun testInstantSerializer() {
        val iso = "2023-01-01T00:00:00Z"
        val instant = iso.toPlatformInstantISO()
        val json = Json.encodeToString(InstantSerializer, instant)
        assertEquals("\"$iso\"", json)
        assertEquals(iso, Json.decodeFromString(InstantSerializer, json).formatISO())
    }

    @Test
    fun testDurationSerializer() {
        val duration = 120.seconds
        val json = Json.encodeToString(DurationSerializer, duration)
        assertEquals("\"PT2M\"", json)
        assertEquals(duration, Json.decodeFromString(DurationSerializer, json))
    }

    @Test
    fun testLocalDateSerializer() {
        val iso = "2023-01-01"
        val localDate = iso.toPlatformLocalDateISO()
        val json = Json.encodeToString(LocalDateSerializer, localDate)
        assertEquals("\"$iso\"", json)
        assertEquals(iso, Json.decodeFromString(LocalDateSerializer, json).formatISO())
    }

    @Test
    fun testLocalDateTimeSerializer() {
        val iso = "2023-01-01T12:00:00"
        val localDateTime = iso.toPlatformLocalDateTimeISO()
        val json = Json.encodeToString(LocalDateTimeSerializer, localDateTime)
        assertEquals("\"$iso\"", json)
        assertEquals(iso, Json.decodeFromString(LocalDateTimeSerializer, json).formatISO())
    }

    @Test
    fun testOffsetDateTimeSerializer() {
        val iso = "2023-01-01T12:00:00+01:00"
        val offsetDateTime = iso.toPlatformOffsetDateTimeISO()
        val json = Json.encodeToString(OffsetDateTimeSerializer, offsetDateTime)
        assertEquals("\"$iso\"", json)
        assertEquals(iso, Json.decodeFromString(OffsetDateTimeSerializer, json).formatISO())
    }
}
