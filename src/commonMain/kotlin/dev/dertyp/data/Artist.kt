@file:UseContextualSerialization(Artist::class, Genre::class, PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("The category of a MusicBrainz artist.")
enum class ArtistType {
    @SerialName("Person") PERSON,
    @SerialName("Group") GROUP,
    @SerialName("Orchestra") ORCHESTRA,
    @SerialName("Choir") CHOIR,
    @SerialName("Character") CHARACTER,
    @SerialName("Other") OTHER,
    @SerialName("Unknown") UNKNOWN
}

@Serializable
@ModelDoc("Contains metadata about a music artist or group.")
data class Artist(
    @FieldDoc("The artist unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The name of the artist.")
    val name: String,
    @FieldDoc("Whether the artist record represents a group of individuals.")
    val isGroup: Boolean,
    @FieldDoc("Collection of sub-artists if this is a group.")
    val artists: List<Artist> = listOf(),
    @FieldDoc("A short biography or description of the artist.")
    val about: String = "",
    @FieldDoc("Collection of genres associated with this artist.")
    val genres: List<Genre> = listOf(),
    @FieldDoc("The artist image unique identifier.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("The MusicBrainz Artist unique identifier.")
    val musicbrainzId: PlatformUUID? = null,
    @FieldDoc("Whether the current user is following this artist.")
    val isFollowed: Boolean = false,
)

@Serializable
@ModelDoc("Represents an alternative name for an artist.")
data class ArtistAlias(
    @FieldDoc("The artist unique identifier.")
    val artistId: PlatformUUID,
    @FieldDoc("The alternative name.")
    val name: String
)

@Serializable
@ModelDoc("Represents a split mapping for a combined artist name.")
data class ArtistSplitAlias(
    @FieldDoc("The artist unique identifier.")
    val artistId: PlatformUUID,
    @FieldDoc("The name to be split.")
    val name: String
)


@Serializable
@ModelDoc("Configuration for merging multiple artist records into one.")
data class MergeArtists(
    @FieldDoc("The target name of the merged artist.")
    val name: String,
    @FieldDoc("The target image URL for the merged artist.")
    val image: String? = null,
    @FieldDoc("Collection of artist IDs to merge.")
    val artistIds: List<PlatformUUID>,
)

@Serializable
@ModelDoc("Configuration for splitting an artist record into multiple artists.")
data class SplitArtist(
    @FieldDoc("The original artist ID to split.")
    val artistId: PlatformUUID,
    @FieldDoc("Map of names to their new artist IDs.")
    val newArtists: Map<String, PlatformUUID?>,
)
