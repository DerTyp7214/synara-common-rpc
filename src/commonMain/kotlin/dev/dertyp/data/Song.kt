@file:UseContextualSerialization(Artist::class, Album::class, Genre::class, Image::class, PlatformUUID::class)
@file:OptIn(ExperimentalSerializationApi::class)

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
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.cbor.CborLabel

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

@Serializable
@ModelDoc("Physical properties of one audio file belonging to a song.")
data class AudioInfo(
    @CborLabel(1)
    @FieldDoc("Audio codec of the file, e.g. flac, wav, eac3.")
    val codec: String,
    @CborLabel(2)
    @FieldDoc("Audio sample rate in Hz.")
    val sampleRate: Int,
    @CborLabel(3)
    @FieldDoc("Number of bits per audio sample; 0 for lossy codecs.")
    val bitsPerSample: Int,
    @CborLabel(4)
    @FieldDoc("Audio bit rate in bits per second.")
    val bitRate: Long,
    @CborLabel(5)
    @FieldDoc("Size of the audio file in bytes.")
    val fileSize: Long,
    @CborLabel(6)
    @FieldDoc("Number of audio channels, e.g. 2 for stereo or 6 for 5.1.")
    val channels: Int,
) {
    companion object {
        val EMPTY = AudioInfo("", 0, 0, 0, 0, 0)
    }
}

const val LEGACY_AUDIO_FIELDS = "API version 3 wire compatibility only; populated by the server's response shaping, never by services. Use audio/atmos."

val BaseSong.effectiveAudio: AudioInfo?
    get() = audio ?: run {
        @Suppress("DEPRECATION")
        if (sampleRate == null && bitsPerSample == null && bitRate == null && fileSize == null) null
        else AudioInfo(
            codec = path.substringAfterLast('.', "").lowercase(),
            sampleRate = sampleRate ?: 0,
            bitsPerSample = bitsPerSample ?: 0,
            bitRate = bitRate ?: 0,
            fileSize = fileSize ?: 0,
            channels = 0,
        )
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
    abstract val audio: AudioInfo?
    abstract val atmos: AudioInfo?
    @Deprecated(LEGACY_AUDIO_FIELDS)
    abstract val sampleRate: Int?
    @Deprecated(LEGACY_AUDIO_FIELDS)
    abstract val bitsPerSample: Int?
    @Deprecated(LEGACY_AUDIO_FIELDS)
    abstract val bitRate: Long?
    @Deprecated(LEGACY_AUDIO_FIELDS)
    abstract val fileSize: Long?
    abstract val coverId: PlatformUUID?
    abstract val blurHash: String?
    abstract val musicBrainzId: PlatformUUID?
    abstract val isrc: String?
    abstract val genres: List<Genre>
    abstract val animatedCoverId: PlatformUUID?
    abstract val animatedCoverImageId: PlatformUUID?
    abstract val animatedCoverBlurHash: String?
    abstract val audioStartMs: Long?
    @Deprecated(LEGACY_AUDIO_FIELDS)
    abstract val atmosPath: String?
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
    @FieldDoc("Properties of the primary audio file.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val audio: AudioInfo? = null,
    @FieldDoc("Properties of the Dolby Atmos (E-AC-3 JOC in MP4) variant, if one exists.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val atmos: AudioInfo? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Audio sample rate in Hz. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val sampleRate: Int? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Number of bits per audio sample. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val bitsPerSample: Int? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Audio bit rate in bits per second. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val bitRate: Long? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Size of the audio file in bytes. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val fileSize: Long? = null,
    @FieldDoc("The song cover image unique identifier.")
    override val coverId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the song cover image.")
    override val blurHash: String? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    override val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("The International Standard Recording Code.")
    override val isrc: String? = null,
    @FieldDoc("Collection of genres associated with this song.")
    override val genres: List<Genre> = listOf(),
    @FieldDoc("The animated cover unique identifier.")
    override val animatedCoverId: PlatformUUID? = null,
    @FieldDoc("Identifier of the still Image from the animated cover's first frame.")
    override val animatedCoverImageId: PlatformUUID? = null,
    @FieldDoc("BlurHash of the animated cover's first frame.")
    override val animatedCoverBlurHash: String? = null,
    @FieldDoc("Offset in milliseconds of the first audible sound, or null if not yet analyzed.")
    override val audioStartMs: Long? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Internal server path to the Dolby Atmos variant. API version 3 only; use atmos and streamSongAtmos.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val atmosPath: String? = null,
    @Transient
    val atmosVariantPath: String? = null,
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
    @FieldDoc("Properties of the primary audio file.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val audio: AudioInfo? = null,
    @FieldDoc("Properties of the Dolby Atmos (E-AC-3 JOC in MP4) variant, if one exists.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val atmos: AudioInfo? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Audio sample rate in Hz. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val sampleRate: Int? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Number of bits per audio sample. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val bitsPerSample: Int? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Audio bit rate in bits per second. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val bitRate: Long? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Size of the audio file in bytes. API version 3 and below only; see audio.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val fileSize: Long? = null,
    @FieldDoc("The song cover image unique identifier.")
    override val coverId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the song cover image.")
    override val blurHash: String? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    override val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("The International Standard Recording Code.")
    override val isrc: String? = null,
    @FieldDoc("Collection of genres associated with this song.")
    override val genres: List<Genre> = listOf(),
    @FieldDoc("The animated cover unique identifier.")
    override val animatedCoverId: PlatformUUID? = null,
    @FieldDoc("Identifier of the still Image from the animated cover's first frame.")
    override val animatedCoverImageId: PlatformUUID? = null,
    @FieldDoc("BlurHash of the animated cover's first frame.")
    override val animatedCoverBlurHash: String? = null,
    @FieldDoc("Offset in milliseconds of the first audible sound, or null if not yet analyzed.")
    override val audioStartMs: Long? = null,
    @Deprecated(LEGACY_AUDIO_FIELDS)
    @FieldDoc("Internal server path to the Dolby Atmos variant. API version 3 only; use atmos and streamSongAtmos.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val atmosPath: String? = null,
    @Transient
    val atmosVariantPath: String? = null,

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
@ModelDoc("Additional audio analysis data for a song.")
data class SongAudioData(
    @FieldDoc("The beats per minute (BPM) of the song.")
    val bpm: Double? = null,
    @FieldDoc("The musical key of the song (e.g. C, G#, Bb).")
    val key: String? = null,
    @FieldDoc("The scale of the song (major or minor).")
    val scale: AudioScale? = null,
    @FieldDoc("The perceived loudness of the track in LUFS.")
    val loudness: Double? = null,
    @FieldDoc("The energy level of the song (0.0 to 1.0).")
    val energy: Double? = null,
    @FieldDoc("The musical positiveness of the song (0.0 to 1.0).")
    val valence: Double? = null,
    @FieldDoc("The danceability level of the song (0.0 to 1.0).")
    val danceability: Double? = null,
    @FieldDoc("The acousticness level of the song (0.0 to 1.0).")
    val acousticness: Double? = null,
    @FieldDoc("The instrumentalness level of the song (0.0 to 1.0).")
    val instrumentalness: Double? = null,
    @FieldDoc("The speechiness level of the song (0.0 to 1.0).")
    val speechiness: Double? = null,
    @FieldDoc("The composer of the song.")
    val composer: List<String>? = null,
    @FieldDoc("The lyricist of the song.")
    val lyricist: List<String>? = null,
    @FieldDoc("The producers of the song.")
    val producers: List<String>? = null,
) {
    companion object {
        const val DEFAULT_BPM = 120.0
        const val DEFAULT_ENERGY = 0.5
        const val DEFAULT_VALENCE = 0.5
        const val DEFAULT_DANCEABILITY = 0.5
        const val DEFAULT_LOUDNESS = -10.0
        const val DEFAULT_ACOUSTICNESS = 0.5
        const val DEFAULT_INSTRUMENTALNESS = 0.5
        const val DEFAULT_SPEECHINESS = 0.5
        const val DEFAULT_DISSONANCE = 0.5
    }
}

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
    @FieldDoc("Properties of the primary audio file.")
    val audio: AudioInfo,
    @FieldDoc("The song cover image unique identifier.")
    val coverId: PlatformUUID?,
    @FieldDoc("The blur hash of the song cover image.")
    val blurHash: String? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("The International Standard Recording Code.")
    val isrc: String? = null,
    @FieldDoc("List of bitrates for which a transcoded version exists.")
    val transcodedTo: List<TranscodedVersion>
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
    @FieldDoc("Properties of the primary audio file.")
    val audio: AudioInfo = AudioInfo.EMPTY,
    @FieldDoc("The hash of the cover image.")
    val coverHash: String? = null,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("The International Standard Recording Code.")
    val isrc: String? = null,
    @FieldDoc("Additional audio analysis data.")
    val audioData: SongAudioData? = null,
    @FieldDoc("Internal server path to the Dolby Atmos (E-AC-3 JOC in MP4) variant, if one exists.")
    val atmosPath: String? = null,
    @FieldDoc("Properties of the Dolby Atmos variant; probed by the server when null and atmosPath is set.")
    val atmos: AudioInfo? = null,
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
