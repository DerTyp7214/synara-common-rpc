package dev.dertyp.core

import dev.dertyp.data.AudioInfo
import dev.dertyp.data.Song
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import dev.dertyp.data.UserSong
import dev.dertyp.platformUUIDFromString
import dev.dertyp.serializers.AppJson
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun testTagsDefaultToEmptyWhenMissing() {
        val json = """{"id":"00000000-0000-0000-0000-000000000000","title":"Title","artists":[],"album":null,"duration":1000,"explicit":false,"path":"path"}"""
        assertEquals(emptyList(), AppJson.decodeFromString<Song>(json).tags)
        assertEquals(emptyList(), AppJson.decodeFromString<UserSong>(json).tags)
    }

    @Test
    fun testEmptyTagsAreOmittedFromJson() {
        val song = Song(id = testId, title = "Title", artists = emptyList(), album = null, duration = 1000, explicit = false, path = "path")
        assertFalse(AppJson.encodeToString(song).contains("\"tags\""))
        val userSong = UserSong(id = testId, title = "Title", artists = emptyList(), album = null, duration = 1000, explicit = false, path = "path")
        assertFalse(AppJson.encodeToString(userSong).contains("\"tags\""))
    }

    @Test
    fun testTagsRoundTrip() {
        val tags = listOf(TitleTag(TitleTagKind.REMIX, "Skrillex Remix"), TitleTag(TitleTagKind.FEAT, "feat. Drake"))
        val song = Song(id = testId, title = "Title", artists = emptyList(), album = null, duration = 1000, explicit = false, path = "path", tags = tags)
        val encoded = AppJson.encodeToString(song)
        assertTrue(encoded.contains("\"tags\""))
        assertEquals(tags, AppJson.decodeFromString<Song>(encoded).tags)
    }
}
