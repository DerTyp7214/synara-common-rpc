@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
sealed class BasePlaylist {
    abstract val id: UUID
    abstract val name: String
    abstract val songs: List<UUID>
    abstract val totalDuration: Long
    abstract val imageId: UUID?
}

@Serializable
data class Playlist(
    override val id: UUID,
    override val name: String,
    override val songs: List<UUID>,
    override val totalDuration: Long = -1L,
    override val imageId: UUID? = null,
): BasePlaylist()

@Serializable
data class UserPlaylist(
    override val id: UUID,
    override val name: String,
    override val songs: List<UUID>,
    override val totalDuration: Long = -1L,
    override val imageId: UUID? = null,
    val creator: UUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: Date? = null,
): BasePlaylist()

@Serializable
data class PlaylistEntry(
    val id: UUID,
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