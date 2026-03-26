package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringTest {

    @Test
    fun testCleanTitle() {
        assertEquals("Song Title", "Song Title (feat. Artist)".cleanTitle())
        assertEquals("Song Title", "Song Title [Explicit]".cleanTitle())
        assertEquals("Song Title", "Song Title (Remastered)".cleanTitle())
        assertEquals("Song Title", "Song Title (Remastered (Explicit))".cleanTitle())
        assertEquals("Song Title", "Song Title (Album Version) (Album Version)".cleanTitle())
        assertEquals("Song Title", "Song Title (Album Version (Explicit))".cleanTitle())
        assertEquals("Song Title", "Song Title (Single Version (Explicit))".cleanTitle())
        assertEquals("Song Title", "Song Title (Live)".cleanTitle())
        assertEquals("Song Title", "Song Title (ft. Someone)".cleanTitle())
        assertEquals("Song Title", "Song Title feat Artist".cleanTitle())
    }

    @Test
    fun testPrefixIfNotBlank() {
        assertEquals("/path", "path".prefixIfNotBlank("/"))
        assertEquals("", "".prefixIfNotBlank("/"))
        assertEquals("   ", "   ".prefixIfNotBlank("/"))
    }

    @Test
    fun testIsURL() {
        assertTrue("https://google.com".isURL())
        assertTrue("ws://localhost:8080".isURL())
        assertFalse("not a url".isURL())
    }

    @Test
    fun testOneLine() {
        assertEquals("Line 1 Line 2", "Line 1\nLine 2".oneLine(" "))
        assertEquals("Line 1Line 2", "Line 1\r\nLine 2".oneLine(""))
    }
}
