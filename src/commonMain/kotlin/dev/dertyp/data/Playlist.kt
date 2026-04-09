@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
sealed class BasePlaylist {
    abstract val id: PlatformUUID
    abstract val name: String
    abstract val songs: List<PlatformUUID>
    abstract val totalDuration: Long
    abstract val imageId: PlatformUUID?
}

@Serializable
@ModelDoc("Represents a system-managed collection of tracks.")
data class Playlist(
    @FieldDoc("The playlist unique identifier.")
    override val id: PlatformUUID,
    @FieldDoc("The name of the playlist.")
    override val name: String,
    @FieldDoc("Collection of song IDs in the playlist.")
    override val songs: List<PlatformUUID>,
    @FieldDoc("Total duration of all tracks in milliseconds.")
    override val totalDuration: Long = -1L,
    @FieldDoc("The playlist cover image unique identifier.")
    override val imageId: PlatformUUID? = null,
): BasePlaylist()

@Serializable
@ModelDoc("Metadata for a single song entry within a user playlist.")
data class UserPlaylistSong(
    @FieldDoc("The song unique identifier.")
    val songId: PlatformUUID,
    @FieldDoc("Unix timestamp of when the song was added.")
    val addedAt: Long,
    @FieldDoc("The MusicBrainz Recording unique identifier.")
    val musicBrainzId: PlatformUUID? = null
)

@Serializable
@ModelDoc("Represents a user-created and managed collection of tracks.")
data class UserPlaylist(
    @FieldDoc("The playlist unique identifier.")
    override val id: PlatformUUID,
    @FieldDoc("The name of the playlist.")
    override val name: String,
    @FieldDoc("Collection of song IDs in the playlist.")
    override val songs: List<PlatformUUID>,
    @FieldDoc("Detailed entry data including added timestamps.")
    val songEntries: List<UserPlaylistSong>? = null,
    @FieldDoc("Total duration of all tracks in milliseconds.")
    override val totalDuration: Long = -1L,
    @FieldDoc("The playlist cover image unique identifier.")
    override val imageId: PlatformUUID? = null,
    @FieldDoc("The unique identifier of the user who created the playlist.")
    val creator: PlatformUUID,
    @FieldDoc("A user-provided description of the playlist.")
    val description: String,
    @FieldDoc("The source or platform where the playlist originated.")
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of the last modification to the playlist.")
    val modifiedAt: PlatformDate? = null,
): BasePlaylist()

@Serializable
@ModelDoc("Contains raw binary data for a cover image in a backup.")
data class BackupImage(
    @FieldDoc("Metadata for the image.")
    val image: Image,
    @FieldDoc("The raw binary data of the image.")
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BackupImage

        if (image != other.image) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@Serializable
@ModelDoc("A complete backup of a user's playlists and their associated images.")
data class UserPlaylistBackup(
    @FieldDoc("The unique identifier of the user.")
    val userId: PlatformUUID,
    @FieldDoc("Collection of user playlists.")
    val playlists: List<UserPlaylist>,
    @FieldDoc("Optional collection of raw images for the playlists.")
    val images: List<BackupImage>? = null
)

@Serializable
@ModelDoc("A simplified representation of a track within a system playlist.")
data class PlaylistEntry(
    @FieldDoc("The song unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The title of the song.")
    val name: String,
    @FieldDoc("Duration of the song in milliseconds.")
    val duration: Long,
)

@Serializable
@ModelDoc("Configuration for creating or updating a user playlist.")
data class InsertablePlaylist(
    @FieldDoc("The name of the playlist.")
    val name: String,
    @FieldDoc("Optional description.")
    val description: String = "",
    @FieldDoc("Collection of file paths for the songs to include.")
    val songPaths: List<String>,
    @FieldDoc("Optional hash of the cover image.")
    val imageHash: String? = null,
    @FieldDoc("Optional source platform identifier.")
    val origin: String? = null,
)
