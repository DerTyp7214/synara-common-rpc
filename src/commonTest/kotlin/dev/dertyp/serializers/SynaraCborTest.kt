@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package dev.dertyp.serializers

import dev.dertyp.data.Album
import dev.dertyp.data.Artist
import dev.dertyp.data.Genre
import dev.dertyp.data.Song
import dev.dertyp.platformUUIDFromString
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SynaraCborTest {
    private val synaraCbor = SynaraCbor(AppCbor)

    private fun createSong(id: String, album: Album?, artists: List<Artist>): Song {
        return Song(
            id = platformUUIDFromString(id),
            title = "Song Title",
            artists = artists,
            album = album,
            duration = 180000,
            explicit = false,
            path = "/path/to/song.mp3",
            genres = listOf(Genre(id = platformUUIDFromString("44444444-4444-4444-4444-444444444444"), name = "Genre"))
        )
    }

    @Test
    fun testDeduplication() = withSynaraPack {
        val artist = Artist(
            id = platformUUIDFromString("11111111-1111-1111-1111-111111111111"),
            name = "Artist Name",
            isGroup = false
        )
        val album = Album(
            id = platformUUIDFromString("22222222-2222-2222-2222-222222222222"),
            name = "Album Name",
            artists = listOf(artist),
            releaseDate = null,
            totalDuration = 2000
        )
        val songs = (1..100).map { i ->
            createSong("33333333-3333-3333-3333-${i.toString().padStart(12, '0')}", album, listOf(artist))
        }
        val serializer = ListSerializer(Song.serializer())
        
        val standardEncoded = AppCbor.encodeToByteArray(serializer, songs)
        val synaraEncoded = synaraCbor.encodeToByteArray(serializer, songs)
        
        println("Standard size: ${standardEncoded.size}")
        println("Synara size: ${synaraEncoded.size}")
        
        assertTrue(synaraEncoded.size < standardEncoded.size, "Synara encoding should be smaller: ${synaraEncoded.size} vs ${standardEncoded.size}")
        
        val decoded = synaraCbor.decodeFromByteArray(serializer, synaraEncoded)
        
        assertEquals(100, decoded.size)
        assertEquals(decoded[0].album?.id, decoded[1].album?.id)
        assertSame(decoded[0].album, decoded[1].album, "Albums should be the same instance")
        assertSame(decoded[0].artists[0], decoded[1].artists[0], "Artists should be the same instance")
        assertSame(decoded[0].genres[0], decoded[1].genres[0], "Genres should be the same instance")
    }

    @Test
    fun testNegotiationSwitch() {
        val artist = Artist(
            id = platformUUIDFromString("11111111-1111-1111-1111-111111111111"),
            name = "Artist Name",
            isGroup = false
        )
        val songs = listOf(
            createSong("33333333-3333-3333-3333-000000000001", null, listOf(artist)),
            createSong("33333333-3333-3333-3333-000000000002", null, listOf(artist))
        )
        val serializer = ListSerializer(Song.serializer())

        withSynaraPack(enabled = false) {
            val encoded = synaraCbor.encodeToByteArray(serializer, songs)
            val standardEncoded = AppCbor.encodeToByteArray(serializer, songs)
            assertEquals(standardEncoded.size, encoded.size, "Size should match standard CBOR when disabled")
            
            val decoded = synaraCbor.decodeFromByteArray(serializer, encoded)
            assertNotSame(decoded[0].artists[0], decoded[1].artists[0], "Artists should NOT be the same instance when disabled")
        }

        withSynaraPack(enabled = true) {
            val encoded = synaraCbor.encodeToByteArray(serializer, songs)
            val standardEncoded = AppCbor.encodeToByteArray(serializer, songs)
            assertTrue(encoded.size < standardEncoded.size, "Size should be smaller when enabled")
            
            val decoded = synaraCbor.decodeFromByteArray(serializer, encoded)
            assertSame(decoded[0].artists[0], decoded[1].artists[0], "Artists SHOULD be the same instance when enabled")
        }
    }
}
