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
    val musicbrainzId: String? = null,
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

@Serializable
data class MusicBrainzArtist(
    val id: String,
    val name: String? = null,
    val type: ArtistType? = null,
    val gender: String? = null,
    val country: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null,
    val disambiguation: String? = null,
    @SerialName("life-span")
    val lifeSpan: MusicBrainzLifeSpan? = null,
    val area: MusicBrainzArea? = null,
    @SerialName("begin-area")
    val beginArea: MusicBrainzArea? = null,
    @SerialName("end-area")
    val endArea: MusicBrainzArea? = null,
    val tags: List<MusicBrainzTag>? = null,
    val genres: List<MusicBrainzGenre>? = null
)

@Serializable
data class MusicBrainzGenre(
    val id: String,
    val name: String,
    val count: Int? = null
)

@Serializable
data class MusicBrainzLifeSpan(
    val begin: String? = null,
    val end: String? = null,
    val ended: Boolean? = null
)

@Serializable
data class MusicBrainzArea(
    val id: String,
    val name: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)

@Serializable
data class MusicBrainzTag(
    val count: Int,
    val name: String
)
