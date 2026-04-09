@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
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
data class Artist(
    val id: PlatformUUID,
    val name: String,
    val isGroup: Boolean,
    val artists: List<Artist> = listOf(),
    val about: String = "",
    val genres: List<Genre> = listOf(),
    val imageId: PlatformUUID? = null,
    val musicbrainzId: PlatformUUID? = null,
    val isFollowed: Boolean = false,
)

@Serializable
data class ArtistAlias(
    val artistId: PlatformUUID,
    val name: String
)

@Serializable
data class ArtistSplitAlias(
    val artistId: PlatformUUID,
    val name: String
)


@Serializable
data class MergeArtists(
    val name: String,
    val image: String? = null,
    val artistIds: List<PlatformUUID>,
)

@Serializable
data class SplitArtist(
    val artistId: PlatformUUID,
    val newArtists: Map<String, PlatformUUID?>,
)
