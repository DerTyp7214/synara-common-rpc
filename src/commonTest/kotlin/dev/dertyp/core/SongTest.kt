package dev.dertyp.core

import dev.dertyp.data.AudioInfo
import dev.dertyp.data.Song
import dev.dertyp.data.UserSong
import dev.dertyp.platformUUIDFromString
import dev.dertyp.serializers.AppJson
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SongTest {

    private val testId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")

    @Test
    fun testSongOmitLyrics() {
        val song = Song(
            id = testId,
            title = "Title",
            artists = emptyList(),
            album = null,
            duration = 1000,
            explicit = false,
            releaseDate = null,
            lyrics = "LALALA",
            path = "path",
            originalUrl = "",
            trackNumber = 1,
            discNumber = 1,
            copyright = "",
            audio = AudioInfo("flac", 44100, 16, 320000, 1000000, 2),
            coverId = null
        )
        
        val omitted = song.omitLyrics()
        assertEquals("", omitted.lyrics)
        assertEquals(song.title, omitted.title)
    }

    @Test
    fun testUserSongOmitLyrics() {
        val song = UserSong(
            id = testId,
            title = "Title",
            artists = emptyList(),
            album = null,
            duration = 1000,
            explicit = false,
            releaseDate = null,
            lyrics = "LALALA",
            path = "path",
            originalUrl = "",
            trackNumber = 1,
            discNumber = 1,
            copyright = "",
            audio = AudioInfo("flac", 44100, 16, 320000, 1000000, 2),
            coverId = null
        )
        
        val omitted = song.omitLyrics()
        assertEquals("", omitted.lyrics)
    }

    @Test
    fun testAudioStartMsDefaultsToNullWhenMissing() {
        val json = """{"id":"00000000-0000-0000-0000-000000000000","title":"Title","artists":[],"album":null,"duration":1000,"explicit":false,"path":"path"}"""
        assertNull(AppJson.decodeFromString<Song>(json).audioStartMs)
        assertNull(AppJson.decodeFromString<UserSong>(json).audioStartMs)
    }

    @Test
    fun testAudioStartMsRoundTrip() {
        val song = Song(id = testId, title = "Title", artists = emptyList(), album = null, duration = 1000, explicit = false, path = "path", audioStartMs = 1234)
        assertEquals(1234, AppJson.decodeFromString<Song>(AppJson.encodeToString(song)).audioStartMs)
    }
}
