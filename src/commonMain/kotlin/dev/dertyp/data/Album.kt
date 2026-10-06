@file:UseContextualSerialization(
    Artist::class,
    ArtistCredit::class,
    Album::class,
    Genre::class,
    Image::class,
    PlatformUUID::class
)
@file:OptIn(ExperimentalSerializationApi::class)

package dev.dertyp.data

import dev.dertyp.PlatformLocalDate
import dev.dertyp.PlatformUUID
import dev.dertyp.core.contentEquals
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.LocalDateSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Contains metadata about a collection of songs released together.")
data class Album(
    @FieldDoc("The album unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The name of the album.")
    val name: String,
    @FieldDoc("Collection of artists credited for this album.")
    val artists: List<ArtistCredit>,
    @FieldDoc("Total number of songs in the album.")
    val songCount: Int = 0,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("The date the album was released.")
    val releaseDate: PlatformLocalDate?,
    @FieldDoc("Sum of all track durations in milliseconds.")
    val totalDuration: Long,
    @FieldDoc("Total file size of all tracks in bytes.")
    val totalSize: Long = 0,
    @FieldDoc("The album cover image unique identifier.")
    val coverId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the album cover image.")
    val blurHash: String? = null,
    @FieldDoc("Collection of genres associated with this album.")
    val genres: List<Genre> = listOf(),
    @FieldDoc("The original ID of the album on external sources.")
    val originalId: String? = null,
    @FieldDoc("The barcode or UPC of the album.")
    val barcode: String? = null,
    @FieldDoc("The MusicBrainz Release unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("The animated cover unique identifier.")
    val animatedCoverId: PlatformUUID? = null,
    @FieldDoc("Identifier of the still Image from the animated cover's first frame.")
    val animatedCoverImageId: PlatformUUID? = null,
    @FieldDoc("BlurHash of the animated cover's first frame.")
    val animatedCoverBlurHash: String? = null,
    @FieldDoc("Edition markers split off the name, e.g. Deluxe Edition, 10th Anniversary or 2011 Remaster, in order of extraction. The name never contains them.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val tags: List<TitleTag> = emptyList(),
    @FieldDoc("Other editions of the same album folded under this entry, such as deluxe, anniversary, remastered, explicit or clean variants. Each keeps its own identifiers and cover. Their own versions lists are always empty. Only filled by calls that return albums grouped into editions.")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val versions: List<Album> = emptyList(),
)

@Serializable
@ModelDoc("Configuration for creating or updating an album record.")
data class InsertableAlbum(
    @FieldDoc("The name of the album.")
    val name: String,
    @FieldDoc("Collection of artist names.")
    val artists: List<String>,
    @Serializable(with = LocalDateSerializer::class)
    @FieldDoc("The date the album was released.")
    val releaseDate: PlatformLocalDate? = null,
    @FieldDoc("Total number of songs in the album.")
    val songCount: Int = 0,
    @FieldDoc("The hash of the album cover image.")
    val coverHash: String? = null,
    @FieldDoc("The original ID of the album on external sources.")
    val originalId: String? = null,
    @FieldDoc("The barcode or UPC of the album.")
    val barcode: String? = null,
    @FieldDoc("The MusicBrainz Release unique identifier.")
    val musicBrainzId: PlatformUUID? = null,
    @FieldDoc("Edition markers. When empty the server splits them off the name.")
    val tags: List<TitleTag> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        return if (other is InsertableAlbum) contentEquals(other) else false
    }

    override fun hashCode(): Int {
        if (originalId != null) return originalId.hashCode()

        var result = name.hashCode()
        result = 31 * result + artists.sorted().hashCode()
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        return result
    }
}
