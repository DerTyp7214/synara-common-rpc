package dev.dertyp.serializers

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.AudioInfo
import dev.dertyp.data.Song
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import dev.dertyp.platformUUIDFromString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class SerializationTest {

    @Test
    fun testJsonSerialization() {
        val songId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")
        val song = createSong(songId)

        val encoded = AppJson.encodeToString(Song.serializer(), song)
        val decoded = AppJson.decodeFromString(Song.serializer(), encoded)

        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
        assertEquals(song.musicBrainzId, decoded.musicBrainzId)
    }

    @Test
    fun testCborSerialization() {
        val songId = platformUUIDFromString("00000000-0000-0000-0000-000000000000")
        val song = createSong(songId)

        val encoded = AppCbor.encodeToHexString(Song.serializer(), song)
        val decoded = AppCbor.decodeFromHexString(Song.serializer(), encoded)

        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
        assertEquals(song.musicBrainzId, decoded.musicBrainzId)
    }

    @Test
    fun testCurrentWireShapeOmitsLegacyAudioFields() {
        val song = createSong(platformUUIDFromString("00000000-0000-0000-0000-000000000000"))
            .copy(atmos = AudioInfo("eac3", 48000, 0, 768000, 17905147, 6), atmosVariantPath = "/x.atmos.m4a")

        val json = AppJson.encodeToString(Song.serializer(), song)
        val keys = AppJson.parseToJsonElement(json).jsonObject.keys

        assertTrue("audio" in keys)
        assertTrue("atmos" in keys)
        assertFalse("sampleRate" in keys)
        assertFalse("bitsPerSample" in keys)
        assertFalse("bitRate" in keys)
        assertFalse("fileSize" in keys)
        assertFalse("atmosPath" in keys)
        assertFalse("atmosVariantPath" in keys)
        assertEquals(song.copy(atmosVariantPath = null), AppJson.decodeFromString(Song.serializer(), json))
    }

    @Test
    fun testAudioInfoCborUsesLabels() {
        val info = AudioInfo("flac", 44100, 16, 320000, 1000000, 2)

        val labelled = AppCbor.encodeToHexString(AudioInfo.serializer(), info)
        val named = Cbor { encodeDefaults = true }.encodeToHexString(AudioInfo.serializer(), info)

        assertEquals(info, AppCbor.decodeFromHexString(AudioInfo.serializer(), labelled))
        assertTrue(labelled.length < named.length, "labelled=$labelled named=$named")
        assertFalse(labelled.contains("73616d706c6552617465"))
        assertTrue(named.contains("73616d706c6552617465"))
    }

    @Test
    fun testAlbumVersionsJsonRoundTrip() {
        val album = createAlbumWithVersions()

        val json = AppJson.encodeToString(Album.serializer(), album)
        val root = AppJson.parseToJsonElement(json).jsonObject
        val versions = root.getValue("versions").jsonArray

        assertEquals(2, versions.size)
        versions.forEach { assertFalse("versions" in it.jsonObject.keys) }
        assertEquals(album, AppJson.decodeFromString(Album.serializer(), json))
    }

    @Test
    fun testAlbumVersionsCborRoundTrip() {
        val album = createAlbumWithVersions()

        val encoded = AppCbor.encodeToHexString(Album.serializer(), album)
        val decoded = AppCbor.decodeFromHexString(Album.serializer(), encoded)

        assertEquals(1, VERSIONS_KEY_HEX.toRegex().findAll(encoded).count())
        assertEquals(album, decoded)
        assertEquals(album.versions.map { it.id }, decoded.versions.map { it.id })
        assertTrue(decoded.versions.all { it.versions.isEmpty() })
    }

    @Test
    fun testAlbumWithoutVersionsOmitsKey() {
        val album = createAlbum("00000000-0000-0000-0000-000000000001", "Album")

        val json = AppJson.encodeToString(Album.serializer(), album)
        val cbor = AppCbor.encodeToHexString(Album.serializer(), album)

        assertFalse("versions" in AppJson.parseToJsonElement(json).jsonObject.keys)
        assertFalse(cbor.contains(VERSIONS_KEY_HEX))
        assertEquals(album, AppJson.decodeFromString(Album.serializer(), json))
        assertEquals(album, AppCbor.decodeFromHexString(Album.serializer(), cbor))
        assertEquals(emptyList(), AppJson.decodeFromString(Album.serializer(), json).versions)
    }

    @Test
    fun testAlbumVersionGroupIdRoundTrip() {
        val groupId = platformUUIDFromString("00000000-0000-0000-0000-0000000000f1")
        val album = createAlbum("00000000-0000-0000-0000-000000000001", "Album").copy(versionGroupId = groupId)

        val json = AppJson.encodeToString(Album.serializer(), album)
        val cbor = AppCbor.encodeToHexString(Album.serializer(), album)

        assertTrue("versionGroupId" in AppJson.parseToJsonElement(json).jsonObject.keys)
        assertTrue(cbor.contains(VERSION_GROUP_ID_KEY_HEX))
        assertEquals(album, AppJson.decodeFromString(Album.serializer(), json))
        assertEquals(album, AppCbor.decodeFromHexString(Album.serializer(), cbor))
        assertEquals(groupId, AppJson.decodeFromString(Album.serializer(), json).versionGroupId)
        assertEquals(groupId, AppCbor.decodeFromHexString(Album.serializer(), cbor).versionGroupId)
    }

    @Test
    fun testAlbumWithoutVersionGroupIdOmitsKey() {
        val album = createAlbum("00000000-0000-0000-0000-000000000001", "Album")

        val json = AppJson.encodeToString(Album.serializer(), album)
        val cbor = AppCbor.encodeToHexString(Album.serializer(), album)

        assertFalse("versionGroupId" in AppJson.parseToJsonElement(json).jsonObject.keys)
        assertFalse(cbor.contains(VERSION_GROUP_ID_KEY_HEX))
        assertEquals(null, AppJson.decodeFromString(Album.serializer(), json).versionGroupId)
        assertEquals(null, AppCbor.decodeFromHexString(Album.serializer(), cbor).versionGroupId)
    }

    private fun createAlbumWithVersions() = createAlbum("00000000-0000-0000-0000-000000000001", "Album").copy(
        versions = listOf(
            createAlbum("00000000-0000-0000-0000-000000000002", "Album").copy(
                tags = listOf(TitleTag(TitleTagKind.VERSION, "Deluxe Edition")),
                coverId = platformUUIDFromString("00000000-0000-0000-0000-00000000000a"),
            ),
            createAlbum("00000000-0000-0000-0000-000000000003", "Album").copy(
                tags = listOf(TitleTag(TitleTagKind.REMASTER, "2011 Remaster")),
                barcode = "0602537518357",
            ),
        )
    )

    private fun createAlbum(id: String, name: String) = Album(
        id = platformUUIDFromString(id),
        name = name,
        artists = emptyList(),
        releaseDate = null,
        totalDuration = 1000,
    )

    private fun createSong(id: PlatformUUID) = Song(
        id = id,
        title = "Title",
        artists = emptyList(),
        album = null,
        duration = 1000,
        explicit = false,
        releaseDate = null,
        lyrics = "",
        path = "path",
        originalUrl = "",
        trackNumber = 1,
        discNumber = 1,
        copyright = "",
        audio = AudioInfo("flac", 44100, 16, 320000, 1000000, 2),
        coverId = null,
        musicBrainzId = platformUUIDFromString("550e8400-e29b-41d4-a716-446655440000")
    )

    private companion object {
        const val VERSIONS_KEY_HEX = "6876657273696f6e73"
        const val VERSION_GROUP_ID_KEY_HEX = "6e76657273696f6e47726f75704964"
    }
}
