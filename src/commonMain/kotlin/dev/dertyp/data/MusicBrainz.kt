@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class MusicBrainzArtist(
    val id: PlatformUUID,
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
    val genres: List<MusicBrainzGenre>? = null,
    val aliases: List<MusicBrainzAlias>? = null,
    @Transient
    val fetchedAt: Long = 0
)

@Serializable
data class MusicBrainzAlias(
    val name: String,
    @SerialName("sort-name")
    val sortName: String,
    val locale: String? = null,
    val type: String? = null,
    val primary: Boolean? = null,
    @SerialName("begin-date")
    val beginDate: String? = null,
    @SerialName("end-date")
    val endDate: String? = null
)

@Serializable
data class MusicBrainzGenre(
    val id: PlatformUUID,
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
    val id: PlatformUUID,
    val name: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)

@Serializable
data class MusicBrainzTag(
    val count: Int,
    val name: String
)

@Serializable
data class MusicBrainzRecording(
    val id: PlatformUUID,
    val title: String? = null,
    @SerialName("artist-credit")
    val artistCredit: List<MusicBrainzArtistCredit>? = null,
    val releases: List<MusicBrainzRelease>? = null,
    val length: Long? = null,
    val tags: List<MusicBrainzTag>? = null,
    val genres: List<MusicBrainzGenre>? = null,
    @Transient
    val fetchedAt: Long = 0
)

@Serializable
data class MusicBrainzArtistCredit(
    val name: String? = null,
    val joinphrase: String? = null,
    val artist: MusicBrainzArtist? = null
)

@Serializable
data class MusicBrainzRelease(
    val id: PlatformUUID,
    val title: String? = null,
    val status: String? = null,
    val quality: String? = null,
    val barcode: String? = null,
    val country: String? = null,
    val date: String? = null,
    val disambiguation: String? = null,
    @SerialName("release-group")
    val releaseGroup: MusicBrainzReleaseGroup? = null,
    val relations: List<MusicBrainzRelation>? = null,
    val tags: List<MusicBrainzTag>? = null,
    val genres: List<MusicBrainzGenre>? = null,
    @SerialName("artist-credit")
    val artistCredit: List<MusicBrainzArtistCredit>? = null,
    val media: List<MusicBrainzMedia>? = null,
    @Transient
    val fetchedAt: Long = 0
)

@Serializable
data class MusicBrainzMedia(
    val format: String? = null,
    @SerialName("track-count")
    val trackCount: Int? = null,
    val tracks: List<MusicBrainzTrack>? = null
)

@Serializable
data class MusicBrainzTrack(
    val id: PlatformUUID,
    val position: Int? = null,
    val number: String? = null,
    val title: String? = null,
    val recording: MusicBrainzRecording? = null
)

@Serializable
data class MusicBrainzReleaseGroup(
    val id: PlatformUUID,
    val title: String,
    @SerialName("primary-type")
    val primaryType: String? = null,
    @SerialName("first-release-date")
    val firstReleaseDate: String? = null,
    val relations: List<MusicBrainzRelation>? = null,
    val tags: List<MusicBrainzTag>? = null,
    val genres: List<MusicBrainzGenre>? = null,
    @Transient
    val fetchedAt: Long = 0
)

@Serializable
data class MusicBrainzRelation(
    val type: String? = null,
    val url: MusicBrainzRelationUrl? = null,
    @SerialName("release_group")
    val releaseGroup: MusicBrainzReleaseGroup? = null
)

@Serializable
data class MusicBrainzRelationUrl(
    val id: PlatformUUID,
    val resource: String
)
