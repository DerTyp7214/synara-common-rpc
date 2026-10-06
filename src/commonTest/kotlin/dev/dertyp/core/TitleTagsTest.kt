package dev.dertyp.core

import dev.dertyp.data.Album
import dev.dertyp.data.AudioInfo
import dev.dertyp.data.InsertableAlbum
import dev.dertyp.data.InsertableSong
import dev.dertyp.data.Song
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import dev.dertyp.platformUUIDFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TitleTagsTest {

    private val positives = listOf(
        Triple("Song (Skrillex Remix)", "Song", listOf(TitleTag(TitleTagKind.REMIX, "Skrillex Remix"))),
        Triple("Song [Extended Mix]", "Song", listOf(TitleTag(TitleTagKind.MIX, "Extended Mix"))),
        Triple("Song [Mix Cut]", "Song", listOf(TitleTag(TitleTagKind.MIX, "Mix Cut"))),
        Triple("Song (Mixed)", "Song", listOf(TitleTag(TitleTagKind.MIX, "Mixed"))),
        Triple("Song - Mix Cut", "Song", listOf(TitleTag(TitleTagKind.MIX, "Mix Cut"))),
        Triple("Song (Live at Wembley)", "Song", listOf(TitleTag(TitleTagKind.LIVE, "Live at Wembley"))),
        Triple("Song (feat. Drake)", "Song", listOf(TitleTag(TitleTagKind.FEAT, "feat. Drake"))),
        Triple("Song (with Artist)", "Song", listOf(TitleTag(TitleTagKind.FEAT, "with Artist"))),
        Triple("Song (prod. Metro Boomin)", "Song", listOf(TitleTag(TitleTagKind.PROD, "prod. Metro Boomin"))),
        Triple("Song - Radio Edit", "Song", listOf(TitleTag(TitleTagKind.EDIT, "Radio Edit"))),
        Triple("Song - 2011 Remaster", "Song", listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster"))),
        Triple(
            "Song - Live - 2011 Remaster",
            "Song",
            listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster"), TitleTag(TitleTagKind.LIVE, "Live")),
        ),
        Triple("Song (Acoustic)", "Song", listOf(TitleTag(TitleTagKind.ACOUSTIC, "Acoustic"))),
        Triple("Song (Unplugged)", "Song", listOf(TitleTag(TitleTagKind.ACOUSTIC, "Unplugged"))),
        Triple("Song (Instrumental)", "Song", listOf(TitleTag(TitleTagKind.INSTRUMENTAL, "Instrumental"))),
        Triple("Song (Cover)", "Song", listOf(TitleTag(TitleTagKind.COVER, "Cover"))),
        Triple("Song (Demo)", "Song", listOf(TitleTag(TitleTagKind.DEMO, "Demo"))),
        Triple("Song (Album Version)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Album Version"))),
        Triple("Song (Take 2)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Take 2"))),
        Triple("Song (Sped Up)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Sped Up"))),
        Triple("Song (Bonus Track)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Bonus Track"))),
        Triple("Song (Level Space Edition)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Level Space Edition"))),
        Triple("Song [Level Space Edition]", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Level Space Edition"))),
        Triple("Song (LEVEL Space Edition)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "LEVEL Space Edition"))),
        Triple("Song (Special Edition)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Special Edition"))),
        Triple("Song [Explicit]", "Song", emptyList()),
        Triple("Song (Clean)", "Song", emptyList()),
        Triple("Song (Remastered (Explicit))", "Song", listOf(TitleTag(TitleTagKind.REMASTER, "Remastered"))),
        Triple("Song (Album Version) (Album Version)", "Song", listOf(TitleTag(TitleTagKind.VERSION, "Album Version"))),
        Triple("Song feat Artist", "Song", listOf(TitleTag(TitleTagKind.FEAT, "feat Artist"))),
        Triple(
            "Song (Radio Edit) (feat. X)",
            "Song",
            listOf(TitleTag(TitleTagKind.EDIT, "Radio Edit"), TitleTag(TitleTagKind.FEAT, "feat. X")),
        ),
        Triple("Song (Clean Bandit Remix)", "Song", listOf(TitleTag(TitleTagKind.REMIX, "Clean Bandit Remix"))),
    )

    private val negatives = listOf(
        "go with the flow",
        "Dance with Somebody",
        "Song (Drift)",
        "Song (Lofty Mix)",
        "Song (Delivery)",
        "Daft Punk - One More Time",
        "Song - Part 2",
        "Mr. Clean",
        "Believe (In Love)",
        "Song (Take Me Home)",
        "Undercover (Of The Night)",
        "Live Wire",
        "Song (Live Wire)",
        "Song (Cover Me)",
        "Song (Edit This)",
        "Song (Version 2.0)",
        "Song (Deep Inside)",
        "Song (Original Sin)",
        "Song (Demolition)",
        "Song (Radio Ga Ga)",
        "Song (Mixed Feelings)",
        "Song (Remixed Emotions)",
        "Song (Mixed Up)",
        "Song (Cut)",
        "Song (Edition of One)",
        "Song (Editions)",
        "(Live)",
    )

    private val albumPositives = listOf(
        Triple(
            "The Divine Feminine (10th Anniversary)",
            "The Divine Feminine",
            listOf(TitleTag(TitleTagKind.VERSION, "10th Anniversary")),
        ),
        Triple("Album (Deluxe)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe"))),
        Triple("Album (Deluxe Edition)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Edition"))),
        Triple("Album (Deluxe Version)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Version"))),
        Triple("Album [Deluxe]", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe"))),
        Triple("Album [Super Deluxe]", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Super Deluxe"))),
        Triple("Album (Super Deluxe Edition)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Super Deluxe Edition"))),
        Triple("Album (Expanded)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Expanded"))),
        Triple("Album (Expanded Edition)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Expanded Edition"))),
        Triple("Album (Special Edition)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Special Edition"))),
        Triple("Album (Anniversary Edition)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Anniversary Edition"))),
        Triple(
            "Album (20th Anniversary Edition)",
            "Album",
            listOf(TitleTag(TitleTagKind.VERSION, "20th Anniversary Edition")),
        ),
        Triple("Album - 2011 Remaster", "Album", listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster"))),
        Triple("Album (Remastered)", "Album", listOf(TitleTag(TitleTagKind.REMASTER, "Remastered"))),
        Triple("Album - Deluxe Edition", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Edition"))),
        Triple(
            "Album (Deluxe Edition) [2011 Remaster]",
            "Album",
            listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Edition"), TitleTag(TitleTagKind.REMASTER, "2011 Remaster")),
        ),
        Triple(
            "Album (Deluxe) - 2011 Remaster",
            "Album",
            listOf(TitleTag(TitleTagKind.VERSION, "Deluxe"), TitleTag(TitleTagKind.REMASTER, "2011 Remaster")),
        ),
        Triple(
            "Album (Live at Wembley) (Deluxe Edition)",
            "Album (Live at Wembley)",
            listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Edition")),
        ),
        Triple("Album (Deluxe) (deluxe)", "Album", listOf(TitleTag(TitleTagKind.VERSION, "Deluxe"))),
    )

    private val albumNegatives = listOf(
        "Live at Wembley",
        "Album (Live at Wembley)",
        "Album (Live)",
        "Album (feat. X)",
        "Album feat. X",
        "Album (Remixes)",
        "Album (Skrillex Remix)",
        "Album - Single",
        "Album - EP",
        "Album (Explicit)",
        "Album [Clean]",
        "Album - Explicit",
        "Album (Acoustic)",
        "Album (Bonus Track)",
        "Album (Sped Up)",
        "Album (Take 2)",
        "Album (Drift)",
        "Album (Happy Anniversary)",
        "Album (special clear vinyl)",
        "(Deluxe)",
    )

    @Test
    fun testSplitAlbumTitleTags() {
        albumPositives.forEach { (input, name, tags) ->
            val split = input.splitAlbumTitleTags()
            assertEquals(name, split.title, "name of \"$input\"")
            assertEquals(tags, split.tags, "tags of \"$input\"")
        }
    }

    @Test
    fun testSplitAlbumTitleTagsKeepsLegitimateNames() {
        albumNegatives.forEach { input ->
            val split = input.splitAlbumTitleTags()
            assertEquals(input, split.title, "name of \"$input\"")
            assertEquals(emptyList(), split.tags, "tags of \"$input\"")
        }
    }

    @Test
    fun testSplitAlbumTitleTagsRoundTrip() {
        albumPositives.forEach { (input, _, _) ->
            val split = input.splitAlbumTitleTags()
            assertEquals(
                split,
                split.title.withTitleTags(split.tags).splitAlbumTitleTags(),
                "round trip of \"$input\"",
            )
        }
    }

    @Test
    fun testClassifyAlbumTitleTag() {
        assertEquals(TitleTagKind.VERSION, classifyAlbumTitleTag("10th Anniversary"))
        assertEquals(TitleTagKind.VERSION, classifyAlbumTitleTag("Anniversary Edition"))
        assertEquals(TitleTagKind.VERSION, classifyAlbumTitleTag("Super Deluxe"))
        assertEquals(TitleTagKind.VERSION, classifyAlbumTitleTag(" Expanded "))
        assertEquals(TitleTagKind.REMASTER, classifyAlbumTitleTag("2011 Remaster"))
        assertNull(classifyAlbumTitleTag(""))
        assertNull(classifyAlbumTitleTag("explicit"))
        assertNull(classifyAlbumTitleTag("special clear vinyl"))
        assertNull(classifyAlbumTitleTag("apple digital master"))
        assertNull(classifyAlbumTitleTag("Urban Outfitters exclusive"))
        assertNull(classifyAlbumTitleTag("Live at Wembley"))
        assertNull(classifyAlbumTitleTag("feat. X"))
    }

    @Test
    fun testSongSplittingIgnoresAlbumOnlyPatterns() {
        val split = "Song (10th Anniversary)".splitTitleTags()
        assertEquals("Song (10th Anniversary)", split.title)
        assertEquals(emptyList(), split.tags)
        assertNull(classifyTitleTag("10th Anniversary"))
        assertNull(classifyTitleTag("Super Deluxe"))
        assertNull(classifyTitleTag("Expanded"))
    }

    @Test
    fun testAlbumFullName() {
        val album = Album(
            id = platformUUIDFromString("00000000-0000-0000-0000-000000000000"),
            name = "The Divine Feminine (10th Anniversary)",
            artists = emptyList(),
            releaseDate = null,
            totalDuration = 1000,
        )

        assertEquals(album.name, album.fullName)

        val split = album.withSplitTitleTags()
        assertEquals("The Divine Feminine", split.name)
        assertEquals(listOf(TitleTag(TitleTagKind.VERSION, "10th Anniversary")), split.tags)
        assertEquals(album.name, split.fullName)
        assertEquals(split, split.withSplitTitleTags())
    }

    @Test
    fun testAlbumWithSplitTitleTags() {
        val album = Album(
            id = platformUUIDFromString("00000000-0000-0000-0000-000000000000"),
            name = "Album (Deluxe Edition) (deluxe edition) - 2011 Remaster",
            artists = emptyList(),
            releaseDate = null,
            totalDuration = 1000,
            tags = listOf(TitleTag(TitleTagKind.VERSION, "DELUXE EDITION")),
        )

        val split = album.withSplitTitleTags()
        assertEquals("Album", split.name)
        assertEquals(
            listOf(TitleTag(TitleTagKind.VERSION, "DELUXE EDITION"), TitleTag(TitleTagKind.REMASTER, "2011 Remaster")),
            split.tags,
        )
        assertEquals(split, split.withSplitTitleTags())

        val untouched = album.copy(name = "Album (Live at Wembley)")
        assertEquals(untouched, untouched.withSplitTitleTags())
    }

    @Test
    fun testInsertableAlbumWithSplitTitleTags() {
        val album = InsertableAlbum(
            name = "Album [Super Deluxe]",
            artists = listOf("Artist"),
            tags = listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster")),
        )

        val split = album.withSplitTitleTags()
        assertEquals("Album", split.name)
        assertEquals(
            listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster"), TitleTag(TitleTagKind.VERSION, "Super Deluxe")),
            split.tags,
        )
        assertEquals(listOf("Artist"), split.artists)
        assertEquals(split.name, split.withSplitTitleTags().name)
        assertEquals(split.tags, split.withSplitTitleTags().tags)

        val untouched = InsertableAlbum(name = "Album (Explicit)", artists = listOf("Artist"))
        assertEquals("Album (Explicit)", untouched.withSplitTitleTags().name)
        assertEquals(emptyList(), untouched.withSplitTitleTags().tags)
    }

    @Test
    fun testSplitTitleTags() {
        positives.forEach { (input, title, tags) ->
            val split = input.splitTitleTags()
            assertEquals(title, split.title, "title of \"$input\"")
            assertEquals(tags, split.tags, "tags of \"$input\"")
        }
    }

    @Test
    fun testSplitTitleTagsKeepsLegitimateTitles() {
        negatives.forEach { input ->
            val split = input.splitTitleTags()
            assertEquals(input, split.title, "title of \"$input\"")
            assertEquals(emptyList(), split.tags, "tags of \"$input\"")
        }
    }

    @Test
    fun testWithTitleTags() {
        val tags = listOf(TitleTag(TitleTagKind.FEAT, "feat. Drake"), TitleTag(TitleTagKind.REMIX, "Skrillex Remix"))
        assertEquals("Song (feat. Drake) (Skrillex Remix)", "Song".withTitleTags(tags))
        assertEquals("Song", "Song".withTitleTags(emptyList()))
    }

    @Test
    fun testSplitTitleTagsRoundTrip() {
        positives.forEach { (input, _, _) ->
            val split = input.splitTitleTags()
            assertEquals(split, split.title.withTitleTags(split.tags).splitTitleTags(), "round trip of \"$input\"")
        }
    }

    @Test
    fun testMergeTitleTagsDedupsCaseInsensitively() {
        val first = listOf(TitleTag(TitleTagKind.REMIX, "Skrillex Remix"))
        val second = listOf(TitleTag(TitleTagKind.REMIX, "skrillex remix"), TitleTag(TitleTagKind.LIVE, "Live"))
        assertEquals(
            listOf(TitleTag(TitleTagKind.REMIX, "Skrillex Remix"), TitleTag(TitleTagKind.LIVE, "Live")),
            first.mergeTitleTags(second),
        )
    }

    @Test
    fun testClassifyTitleTag() {
        assertEquals(TitleTagKind.REMIX, classifyTitleTag("Skrillex Remix"))
        assertEquals(TitleTagKind.MIX, classifyTitleTag("Club Mix"))
        assertEquals(null, classifyTitleTag("Lofty Mix"))
        assertEquals(null, classifyTitleTag(""))
        assertEquals(TitleTagKind.VERSION, classifyTitleTag("Anniversary Edition"))
    }

    @Test
    fun testInsertableSongWithSplitTitleTags() {
        val song = InsertableSong(
            title = "Song (Skrillex Remix)",
            album = InsertableAlbum(name = "Album", artists = listOf("Artist")),
            duration = 1000,
            explicit = false,
            path = "path",
            tags = listOf(TitleTag(TitleTagKind.LIVE, "Live")),
        )

        val split = song.withSplitTitleTags()
        assertEquals("Song", split.title)
        assertEquals(
            listOf(TitleTag(TitleTagKind.LIVE, "Live"), TitleTag(TitleTagKind.REMIX, "Skrillex Remix")),
            split.tags,
        )
        assertEquals(split, split.withSplitTitleTags())
        assertEquals(split.tags, split.withSplitTitleTags().tags)
    }

    @Test
    fun testWithSplitTitleTagsStripsMarkersWithoutTags() {
        val insertable = InsertableSong(
            title = "Song [Explicit]",
            album = InsertableAlbum(name = "Album", artists = listOf("Artist")),
            duration = 1000,
            explicit = true,
            path = "path",
            tags = listOf(TitleTag(TitleTagKind.LIVE, "Live")),
        )

        val splitInsertable = insertable.withSplitTitleTags()
        assertEquals("Song", splitInsertable.title)
        assertEquals(listOf(TitleTag(TitleTagKind.LIVE, "Live")), splitInsertable.tags)
        assertTrue(splitInsertable.explicit)

        val song = Song(
            id = platformUUIDFromString("00000000-0000-0000-0000-000000000000"),
            title = "Song [Explicit]",
            artists = emptyList(),
            album = null,
            duration = 1000,
            explicit = true,
            path = "path",
            audio = AudioInfo.EMPTY,
            tags = listOf(TitleTag(TitleTagKind.LIVE, "Live")),
        )

        val splitSong = song.withSplitTitleTags()
        assertEquals("Song", splitSong.title)
        assertEquals(listOf(TitleTag(TitleTagKind.LIVE, "Live")), splitSong.tags)
        assertTrue(splitSong.explicit)
    }

    @Test
    fun testWithSplitTitleTagsKeepsCleanTitles() {
        val song = Song(
            id = platformUUIDFromString("00000000-0000-0000-0000-000000000000"),
            title = "Song (Drift)",
            artists = emptyList(),
            album = null,
            duration = 1000,
            explicit = false,
            path = "path",
            audio = AudioInfo.EMPTY,
        )

        assertEquals(song, song.withSplitTitleTags())
    }

    @Test
    fun testSongWithSplitTitleTags() {
        val song = Song(
            id = platformUUIDFromString("00000000-0000-0000-0000-000000000000"),
            title = "Song (Live at Wembley) (feat. Drake)",
            artists = emptyList(),
            album = null,
            duration = 1000,
            explicit = false,
            path = "path",
            audio = AudioInfo.EMPTY,
        )

        val split = song.withSplitTitleTags()
        assertEquals("Song", split.title)
        assertEquals(
            listOf(TitleTag(TitleTagKind.LIVE, "Live at Wembley"), TitleTag(TitleTagKind.FEAT, "feat. Drake")),
            split.tags,
        )
        assertEquals(split, split.withSplitTitleTags())
        assertTrue(split.withSplitTitleTags().tags.size == 2)
        assertEquals(song.title, split.fullTitle)
    }
}
