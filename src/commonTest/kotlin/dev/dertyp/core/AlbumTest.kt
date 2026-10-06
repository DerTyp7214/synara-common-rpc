package dev.dertyp.core

import dev.dertyp.data.InsertableAlbum
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

    @Test
    fun testInsertableAlbumTagsDecideNameBasedIdentity() {
        val plain = InsertableAlbum(name = "Album", artists = listOf("Artist"))
        val anniversary = plain.copy(tags = listOf(TitleTag(TitleTagKind.VERSION, "10th Anniversary")))
        val sameAnniversary = plain.copy(tags = listOf(TitleTag(TitleTagKind.VERSION, "10th Anniversary")))
        val deluxe = plain.copy(tags = listOf(TitleTag(TitleTagKind.VERSION, "Deluxe")))

        assertFalse(plain.contentEquals(anniversary))
        assertFalse(anniversary.contentEquals(deluxe))
        assertNotEquals(plain, anniversary)
        assertNotEquals(anniversary, deluxe)
        assertNotEquals(plain.hashCode(), anniversary.hashCode())
        assertNotEquals(anniversary.hashCode(), deluxe.hashCode())

        assertTrue(anniversary.contentEquals(sameAnniversary))
        assertEquals(anniversary, sameAnniversary)
        assertEquals(anniversary.hashCode(), sameAnniversary.hashCode())
        assertEquals(2, setOf(plain, anniversary, sameAnniversary).size)
    }

    @Test
    fun testInsertableAlbumTagsDoNotAffectOriginalIdIdentity() {
        val plain = InsertableAlbum(name = "Album", artists = listOf("Artist"), originalId = "123")
        val anniversary = plain.copy(tags = listOf(TitleTag(TitleTagKind.VERSION, "10th Anniversary")))
        val otherId = anniversary.copy(originalId = "456")

        assertTrue(plain.contentEquals(anniversary))
        assertEquals(plain, anniversary)
        assertEquals(plain.hashCode(), anniversary.hashCode())

        assertFalse(anniversary.contentEquals(otherId))
        assertNotEquals(anniversary, otherId)
    }
}
