package dev.dertyp.serializers

import dev.dertyp.data.Song
import dev.dertyp.platformUUIDFromString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlin.test.Test
import kotlin.test.assertEquals

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
    }

    @Test
    fun testCborSerialization() {
        val songId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")
        val song = createSong(songId)
        
        val encoded = AppCbor.encodeToHexString(Song.serializer(), song)
        val decoded = AppCbor.decodeFromHexString(Song.serializer(), encoded)
        
        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
    }

    private fun createSong(id: dev.dertyp.PlatformUUID) = Song(
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
        sampleRate = 44100,
        bitsPerSample = 16,
        bitRate = 320000,
        fileSize = 1000000,
        coverId = null
    )
}
