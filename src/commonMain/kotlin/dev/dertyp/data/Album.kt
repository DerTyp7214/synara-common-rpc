@file:UseContextualSerialization(Artist::class, Album::class, Genre::class, Image::class, PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformLocalDate
import dev.dertyp.PlatformUUID
import dev.dertyp.core.contentEquals
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.LocalDateSerializer
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
    val artists: List<Artist>,
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
    @FieldDoc("Collection of genres associated with this album.")
    val genres: List<Genre> = listOf(),
    @FieldDoc("The original ID of the album on external sources.")
    val originalId: String? = null,
    @FieldDoc("The MusicBrainz Release unique identifier.")
    val musicbrainzId: PlatformUUID? = null,
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
) {
    override fun equals(other: Any?): Boolean {
        return if (other is InsertableAlbum) contentEquals(other) else false
    }

    override fun hashCode(): Int {
        var result = songCount
        result = 31 * result + name.hashCode()
        result = 31 * result + artists.sorted().joinToString(", ").hashCode()
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (originalId?.hashCode() ?: 0)
        return result
    }
}
