@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package dev.dertyp.serializers

import dev.dertyp.data.*
import dev.dertyp.platformUUIDFromString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelConsistencyTest {
    private val synaraCbor = AppCbor

    @Test
    fun testMusicBrainzSerialization() = runTest {
        val recording = MusicBrainzRecording(
            id = platformUUIDFromString("55555555-5555-5555-5555-555555555555"),
            title = "MB Title",
            artistCredit = listOf(MusicBrainzArtistCredit(name = "MB Artist"))
        )
        
        val bytes = synaraCbor.encodeToByteArray(MusicBrainzRecording.serializer(), recording)
        val decoded = synaraCbor.decodeFromByteArray(MusicBrainzRecording.serializer(), bytes)
        
        assertEquals(recording.id, decoded.id)
        assertEquals(recording.title, decoded.title)
        assertEquals(recording.artistCredit?.first()?.name, decoded.artistCredit?.first()?.name)
    }

    @Test
    fun testSongWithDeduplication() = runTest {
        val artist = Artist(
            id = platformUUIDFromString("11111111-1111-1111-1111-111111111111"),
            name = "Artist",
            isGroup = false
        )
        val album = Album(
            id = platformUUIDFromString("22222222-2222-2222-2222-222222222222"),
            name = "Album",
            artists = listOf(artist),
            releaseDate = null,
            totalDuration = 1000
        )
        val song = Song(
            id = platformUUIDFromString("33333333-3333-3333-3333-333333333333"),
            title = "Song",
            artists = listOf(artist),
            album = album,
            duration = 1000,
            explicit = false,
            path = "/test.flac"
        )

        val bytes = synaraCbor.encodeToByteArray(Song.serializer(), song)
        val decoded = synaraCbor.decodeFromByteArray(Song.serializer(), bytes)

        assertEquals(song.id, decoded.id)
        assertEquals(song.album?.name, decoded.album?.name)
        assertEquals(song.artists.first().name, decoded.artists.first().name)
    }
}
