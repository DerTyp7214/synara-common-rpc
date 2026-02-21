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
data class UserPlaylist(
    override val id: PlatformUUID,
    override val name: String,
    override val songs: List<PlatformUUID>,
    override val totalDuration: Long = -1L,
    override val imageId: PlatformUUID? = null,
    val creator: PlatformUUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: PlatformDate? = null,
): BasePlaylist()

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
