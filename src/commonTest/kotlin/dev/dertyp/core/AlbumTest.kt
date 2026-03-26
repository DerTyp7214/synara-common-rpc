package dev.dertyp.core

import dev.dertyp.data.InsertableAlbum
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlbumTest {

    @Test
    fun testInsertableAlbumContentEquals() {
        val album1 = InsertableAlbum(
            name = "Album",
            artists = listOf("Artist B", "Artist A"),
            releaseDate = null
        )
        
        val album2 = InsertableAlbum(
            name = "Album",
            artists = listOf("Artist A", "Artist B"),
            releaseDate = null
        )
        
        val album3 = InsertableAlbum(
            name = "Different",
            artists = listOf("Artist A", "Artist B"),
            releaseDate = null
        )
        
        assertTrue(album1.contentEquals(album2))
        assertFalse(album1.contentEquals(album3))
    }
}
