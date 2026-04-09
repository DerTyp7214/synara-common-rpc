@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
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
data class Playlist(
    override val id: PlatformUUID,
    override val name: String,
    override val songs: List<PlatformUUID>,
    override val totalDuration: Long = -1L,
    override val imageId: PlatformUUID? = null,
): BasePlaylist()

@Serializable
data class UserPlaylistSong(
    val songId: PlatformUUID,
    val addedAt: Long,
    val musicBrainzId: PlatformUUID? = null
)

@Serializable
data class UserPlaylist(
    override val id: PlatformUUID,
    override val name: String,
    override val songs: List<PlatformUUID>,
    val songEntries: List<UserPlaylistSong>? = null,
    override val totalDuration: Long = -1L,
    override val imageId: PlatformUUID? = null,
    val creator: PlatformUUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: PlatformDate? = null,
): BasePlaylist()

@Serializable
data class BackupImage(
    val image: Image,
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
data class UserPlaylistBackup(
    val userId: PlatformUUID,
    val playlists: List<UserPlaylist>,
    val images: List<BackupImage>? = null
)

@Serializable
data class PlaylistEntry(
    val id: PlatformUUID,
    val name: String,
    val duration: Long,
)

@Serializable
data class InsertablePlaylist(
    val name: String,
    val description: String = "",
    val songPaths: List<String>,
    val imageHash: String? = null,
    val origin: String? = null,
)
