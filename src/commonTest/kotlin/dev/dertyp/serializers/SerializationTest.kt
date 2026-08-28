package dev.dertyp.serializers

import dev.dertyp.PlatformUUID
import dev.dertyp.data.AudioInfo
import dev.dertyp.data.Song
import dev.dertyp.platformUUIDFromString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class SerializationTest {

    @Test
    fun testJsonSerialization() {
        val songId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")
        val song = createSong(songId)
        
        val encoded = AppJson.encodeToString(Song.serializer(), song)
        val decoded = AppJson.decodeFromString(Song.serializer(), encoded)
        
        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
        assertEquals(song.musicBrainzId, decoded.musicBrainzId)
    }

    @Test
    fun testCborSerialization() {
        val songId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")
        val song = createSong(songId)
        
        val encoded = AppCbor.encodeToHexString(Song.serializer(), song)
        val decoded = AppCbor.decodeFromHexString(Song.serializer(), encoded)
        
        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
        assertEquals(song.musicBrainzId, decoded.musicBrainzId)
    }

    @Test
    fun testCurrentWireShapeOmitsLegacyAudioFields() {
        val song = createSong(platformUUIDFromString("00000000-0000-0000-0000-000000000000"))
            .copy(atmos = AudioInfo("eac3", 48000, 0, 768000, 17905147, 6), atmosVariantPath = "/x.atmos.m4a")

        val json = AppJson.encodeToString(Song.serializer(), song)
        val keys = AppJson.parseToJsonElement(json).jsonObject.keys

        assertTrue("audio" in keys)
        assertTrue("atmos" in keys)
        assertFalse("sampleRate" in keys)
        assertFalse("bitsPerSample" in keys)
        assertFalse("bitRate" in keys)
        assertFalse("fileSize" in keys)
        assertFalse("atmosPath" in keys)
        assertFalse("atmosVariantPath" in keys)
        assertEquals(song.copy(atmosVariantPath = null), AppJson.decodeFromString(Song.serializer(), json))
    }

    @Test
    @Suppress("DEPRECATION")
    fun testLegacyWireShapeOmitsAudioInfo() {
        val song = createSong(platformUUIDFromString("00000000-0000-0000-0000-000000000000"))
            .copy(audio = null, sampleRate = 44100, bitsPerSample = 16, bitRate = 320000, fileSize = 1000000, atmosPath = "/x.atmos.m4a")

        val json = AppJson.encodeToString(Song.serializer(), song)
        val obj = AppJson.parseToJsonElement(json).jsonObject

        assertFalse("audio" in obj.keys)
        assertFalse("atmos" in obj.keys)
        assertEquals("44100", obj["sampleRate"].toString())
        assertEquals("16", obj["bitsPerSample"].toString())
        assertEquals("320000", obj["bitRate"].toString())
        assertEquals("1000000", obj["fileSize"].toString())
        assertEquals("\"/x.atmos.m4a\"", obj["atmosPath"].toString())
    }

    @Test
    fun testAudioInfoCborUsesLabels() {
        val info = AudioInfo("flac", 44100, 16, 320000, 1000000, 2)

        val labelled = AppCbor.encodeToHexString(AudioInfo.serializer(), info)
        val named = Cbor { encodeDefaults = true }.encodeToHexString(AudioInfo.serializer(), info)

        assertEquals(info, AppCbor.decodeFromHexString(AudioInfo.serializer(), labelled))
        assertTrue(labelled.length < named.length, "labelled=$labelled named=$named")
        assertFalse(labelled.contains("73616d706c6552617465"))
        assertTrue(named.contains("73616d706c6552617465"))
    }

    private fun createSong(id: PlatformUUID) = Song(
        id = id,
        title = "Title",
        artists = emptyList(),
        album = null,
        duration = 1000,
        explicit = false,
        releaseDate = null,
        lyrics = "",
        path = "path",
        originalUrl = "",
        trackNumber = 1,
        discNumber = 1,
        copyright = "",
        audio = AudioInfo("flac", 44100, 16, 320000, 1000000, 2),
        coverId = null,
        musicBrainzId = platformUUIDFromString("550e8400-e29b-41d4-a716-446655440000")
    )
}
