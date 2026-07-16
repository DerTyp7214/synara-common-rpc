@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("The kind of entity configured on a radio channel.")
enum class RadioChannelItemType {
    @FieldDoc("A single song.")
    SONG,
    @FieldDoc("A whole album.")
    ALBUM,
    @FieldDoc("A whole artist.")
    ARTIST
}

@Serializable
@ModelDoc("An admin-curated radio station scoped to a configured set of songs, artists and albums.")
data class RadioChannel(
    @FieldDoc("The channel unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The channel name.")
    val name: String,
    @FieldDoc("An optional description.")
    @property:ObjCName("channelDescription") val description: String? = null,
    @FieldDoc("The optional cover image unique identifier.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the cover image.")
    val blurHash: String? = null,
    @FieldDoc("Whether the channel is published and visible to non-admin users.")
    val enabled: Boolean = false,
    @FieldDoc("Display ordering; lower values appear first.")
    val position: Int = 0,
    @FieldDoc("When true the channel expands its configured content into recommended similar songs; when false it plays only the configured content.")
    val discovery: Boolean = false,
    @FieldDoc("Number of songs explicitly configured on the channel.")
    val songCount: Int = 0,
    @FieldDoc("Number of artists configured on the channel.")
    val artistCount: Int = 0,
    @FieldDoc("Number of albums configured on the channel.")
    val albumCount: Int = 0,
)

@Serializable
@ModelDoc("A song matched by a radio channel search, with how it belongs to the channel.")
data class RadioChannelSongMatch(
    @FieldDoc("The matched song.")
    val song: UserSong,
    @FieldDoc("True when the song is added directly to the channel; false when it is reached via an album or artist that is configured on the channel.")
    val explicitMember: Boolean,
)

@Serializable
@ModelDoc("Ranked search results within a single radio channel's configured content, grouped by entity type.")
data class RadioChannelSearchResults(
    @FieldDoc("Matching songs the channel resolves to (explicit and implicit members).")
    val songs: PaginatedResponse<RadioChannelSongMatch>,
    @FieldDoc("Matching artists configured on the channel.")
    val artists: PaginatedResponse<Artist>,
    @FieldDoc("Matching albums configured on the channel.")
    val albums: PaginatedResponse<Album>,
)

@Serializable
@ModelDoc("Configuration for creating or updating a radio channel's metadata.")
data class InsertableRadioChannel(
    @FieldDoc("The channel name.")
    val name: String,
    @FieldDoc("An optional description.")
    @property:ObjCName("channelDescription") val description: String? = null,
    @FieldDoc("Whether the channel is published and visible to non-admin users.")
    val enabled: Boolean = false,
    @FieldDoc("Display ordering; lower values appear first.")
    val position: Int = 0,
    @FieldDoc("When true the channel expands its configured content into recommended similar songs.")
    val discovery: Boolean = false,
)
