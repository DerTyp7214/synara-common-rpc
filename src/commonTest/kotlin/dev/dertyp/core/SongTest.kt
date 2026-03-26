package dev.dertyp.core

import dev.dertyp.data.Song
import dev.dertyp.data.UserSong
import dev.dertyp.platformUUIDFromString
import kotlin.test.Test
import kotlin.test.assertEquals

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
            sampleRate = 44100,
            bitsPerSample = 16,
            bitRate = 320000,
            fileSize = 1000000,
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
            sampleRate = 44100,
            bitsPerSample = 16,
            bitRate = 320000,
            fileSize = 1000000,
            coverId = null
        )
        
        val omitted = song.omitLyrics()
        assertEquals("", omitted.lyrics)
    }
}
