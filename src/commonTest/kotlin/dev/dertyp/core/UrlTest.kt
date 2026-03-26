package dev.dertyp.core

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("HttpUrlsUsage")
class UrlTest {

    @Test
    fun testTidalId() {
        val url = Url("https://listen.tidal.com/album/12345")
        assertEquals("12345", url.tidalId())
        
        val userUrl = Url("https://listen.tidal.com/album/12345/u")
        assertEquals("12345", userUrl.tidalId())
    }

    @Test
    fun testSafeParseUrl() {
        assertEquals("https://google.com", safeParseUrl("https://google.com")?.toString())
        assertNull(safeParseUrl("not a url"))
        assertNull(safeParseUrl("http://"))
    }
}
