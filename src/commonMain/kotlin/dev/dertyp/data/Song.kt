@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformLocalDate
import dev.dertyp.PlatformUUID
import dev.dertyp.core.contentEquals
import dev.dertyp.nowAsPlatformDate
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import dev.dertyp.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Flags and metadata attributes for a song.")
enum class SongTag {
    @FieldDoc("Audio sample rate is 44.1kHz or 48kHz.") Q_44_48,
    @FieldDoc("Audio sample rate is 96kHz.") Q_96,
    @FieldDoc("Audio sample rate is 192kHz.") Q_192,
    @FieldDoc("Bit depth is 16-bit.") B_16,
    @FieldDoc("Bit depth is 24-bit.") B_24,
    @FieldDoc("The song has associated lyrics.") HAS_LYRICS,
    @FieldDoc("The song was manually uploaded by a user.") CUSTOM_UPLOAD,
    @FieldDoc("The song has a linked MusicBrainz Recording ID.") HAS_MUSICBRAINZ_ID
}

abstract class BaseSong() {
    abstract val id: PlatformUUID
    abstract val title: String
    abstract val artists: List<Artist>
    abstract val album: Album?
    abstract val duration: Long
    abstract val explicit: Boolean
    @Serializable(with = LocalDateSerializer::class)
    abstract val releaseDate: PlatformLocalDate?
    abstract val lyrics: String
    abstract val path: String
    abstract val originalUrl: String
    abstract val trackNumber: Int
    abstract val discNumber: Int
    abstract val copyright: String
    abstract val sampleRate: Int
    abstract val bitsPerSample: Int
    abstract val bitRate: Long
    abstract val fileSize: Long
    abstract val coverId: PlatformUUID?
    abstract val musicBrainzId: PlatformUUID?
    abstract val genres: List<Genre>
}

@Serializable
@ModelDoc("Contains core metadata about a track that is common for all users.")
data class Song(
    @FieldDoc("The song unique identifier.")
    override val id: PlatformUUID,
    @FieldDoc("The title of the song.")
    override val title: String,
    @FieldDoc("Collection of performing artists.")
    override val artists: List<Artist>,
    @FieldDoc("The album this song belongs to.")
    override val album: Album?,
    @FieldDoc("Duration of the song in milliseconds.")
    override val duration: Long,
    @FieldDoc("Whether the song contains explicit content.")
    override val explicit: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("Original release date of the track.")
    override val releaseDate: PlatformLocalDate? = null,
    @FieldDoc("The plain text lyrics of the song.")
    override val lyrics: String = "",
    @FieldDoc("Internal server path to the audio file.")
    override val path: String,
    @FieldDoc("The URL to the item on its original platform.")
    override val originalUrl: String = "",
    @FieldDoc("The position of the track in the album.")
    override val trackNumber: Int = 1,
    @FieldDoc("The disc number in a multi-disc album.")
    override val discNumber: Int = 1,
    @FieldDoc("Copyright information for the track.")
    override val copyright: String = "",
    @FieldDoc("Audio sample rate in Hz.")
    override val sampleRate: Int = 0,
    @FieldDoc("Number of bits per audio sample.")
    override val bitsPerSample: Int = 0,
    @FieldDoc("Audio bit rate in bits per second.")
    override val bitRate: Long = 0,
    @FieldDoc("Size of the audio file in bytes.")
    override val fileSize: Long = 0,
    @FieldDoc("The song cover image unique identifier.")
    override val coverId: PlatformUUID? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    override val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("Collection of genres associated with this song.")
    override val genres: List<Genre> = listOf(),
): BaseSong()

@Serializable
@ModelDoc("Extends track metadata with user-specific information like favorite status.")
data class UserSong(
    @FieldDoc("The song unique identifier.")
    override val id: PlatformUUID,
    @FieldDoc("The title of the song.")
    override val title: String,
    @FieldDoc("Collection of performing artists.")
    override val artists: List<Artist>,
    @FieldDoc("The album this song belongs to.")
    override val album: Album?,
    @FieldDoc("Duration of the song in milliseconds.")
    override val duration: Long,
    @FieldDoc("Whether the song contains explicit content.")
    override val explicit: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("Original release date of the track.")
    override val releaseDate: PlatformLocalDate? = null,
    @FieldDoc("The plain text lyrics of the song.")
    override val lyrics: String = "",
    @FieldDoc("Internal server path to the audio file.")
    override val path: String,
    @FieldDoc("The URL to the item on its original platform.")
    override val originalUrl: String = "",
    @FieldDoc("The position of the track in the album.")
    override val trackNumber: Int = 1,
    @FieldDoc("The disc number in a multi-disc album.")
    override val discNumber: Int = 1,
    @FieldDoc("Copyright information for the track.")
    override val copyright: String = "",
    @FieldDoc("Audio sample rate in Hz.")
    override val sampleRate: Int = 0,
    @FieldDoc("Number of bits per audio sample.")
    override val bitsPerSample: Int = 0,
    @FieldDoc("Audio bit rate in bits per second.")
    override val bitRate: Long = 0,
    @FieldDoc("Size of the audio file in bytes.")
    override val fileSize: Long = 0,
    @FieldDoc("The song cover image unique identifier.")
    override val coverId: PlatformUUID? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    override val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("Collection of genres associated with this song.")
    override val genres: List<Genre> = listOf(),

    @FieldDoc("Whether the current user has marked this song as a favorite.")
    val isFavourite: Boolean? = false,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of when the song record was created.")
    val userSongCreatedAt: PlatformDate? = nowAsPlatformDate(),
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of the last update to the song metadata.")
    val userSongUpdatedAt: PlatformDate? = nowAsPlatformDate(),
): BaseSong()

@Serializable
@ModelDoc("A simplified representation of a song.")
data class SimpleSong(
    @FieldDoc("The song unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The title of the song.")
    val title: String,
    @FieldDoc("Duration of the song in milliseconds.")
    val duration: Long,
    @FieldDoc("Whether the song contains explicit content.")
    val explicit: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("Original release date of the track.")
    val releaseDate: PlatformLocalDate?,
    @FieldDoc("Internal server path to the audio file.")
    val path: String,
    @FieldDoc("The URL to the item on its original platform.")
    val originalUrl: String,
    @FieldDoc("The position of the track in the album.")
    val trackNumber: Int,
    @FieldDoc("The disc number in a multi-disc album.")
    val discNumber: Int,
    @FieldDoc("Audio sample rate in Hz.")
    val sampleRate: Int,
    @FieldDoc("Number of bits per audio sample.")
    val bitsPerSample: Int,
    @FieldDoc("Audio bit rate in bits per second.")
    val bitRate: Long,
    @FieldDoc("Size of the audio file in bytes.")
    val fileSize: Long,
    @FieldDoc("The song cover image unique identifier.")
    val coverId: PlatformUUID?,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("List of bitrates for which a transcoded version exists.")
    val transcodedTo: List<Int>
)

@Serializable
@ModelDoc("Configuration for creating or updating a song record.")
data class InsertableSong(
    @FieldDoc("The title of the song.")
    val title: String,
    @FieldDoc("Collection of artist names.")
    val artists: List<String> = listOf(),
    @FieldDoc("The album configuration.")
    val album: InsertableAlbum,
    @FieldDoc("Duration of the song in milliseconds.")
    val duration: Long,
    @FieldDoc("Whether the song contains explicit content.")
    val explicit: Boolean,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("Original release date of the track.")
    val releaseDate: PlatformLocalDate? = null,
    @FieldDoc("The plain text lyrics of the song.")
    val lyrics: String = "",
    @FieldDoc("Internal server path to the audio file.")
    val path: String,
    @FieldDoc("The URL to the item on its original platform.")
    val originalUrl: String = "",
    @FieldDoc("The position of the track in the album.")
    val trackNumber: Int = 1,
    @FieldDoc("The disc number in a multi-disc album.")
    val discNumber: Int = 1,
    @FieldDoc("Copyright information for the track.")
    val copyright: String = "",
    @FieldDoc("Audio sample rate in Hz.")
    val sampleRate: Int = 0,
    @FieldDoc("Number of bits per audio sample.")
    val bitsPerSample: Int = 0,
    @FieldDoc("Audio bit rate in bits per second.")
    val bitRate: Long = 0,
    @FieldDoc("Size of the audio file in bytes.")
    val fileSize: Long = 0,
    @FieldDoc("The hash of the cover image.")
    val coverHash: String? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
) {
    override fun equals(other: Any?): Boolean {
        return if (other is InsertableSong) contentEquals(other) else false
    }

    override fun hashCode(): Int {
        var result = trackNumber
        result = 31 * result + title.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + discNumber.hashCode()
        result = 31 * result + album.name.hashCode()
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        return result
    }
}
