package dev.dertyp.core

import dev.dertyp.data.AudioInfo
import dev.dertyp.data.InsertableAlbum
import dev.dertyp.data.InsertableSong
import dev.dertyp.data.Song
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import dev.dertyp.platformUUIDFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TitleTagsTest {

    private val positives = listOf(
        Triple("Song (Skrillex Remix)", "Song", listOf(TitleTag(TitleTagKind.REMIX, "Skrillex Remix"))),
        Triple("Song [Extended Mix]", "Song", listOf(TitleTag(TitleTagKind.MIX, "Extended Mix"))),
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
        "(Live)",
    )

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
