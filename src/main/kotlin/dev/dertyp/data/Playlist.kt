package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
sealed class BasePlaylist {
    abstract val id: @Contextual UUID
    abstract val name: String
    abstract val songs: List<@Contextual UUID>
    abstract val totalDuration: Long
    abstract val imageId: @Contextual UUID?
}

@Serializable
data class Playlist(
    override val id: @Contextual UUID,
    override val name: String,
    override val songs: List<@Contextual UUID>,
    override val totalDuration: Long = -1L,
    override val imageId: @Contextual UUID? = null,
): BasePlaylist()

@Serializable
data class UserPlaylist(
    override val id: @Contextual UUID,
    override val name: String,
    override val songs: List<@Contextual UUID>,
    override val totalDuration: Long = -1L,
    override val imageId: @Contextual UUID? = null,
    val creator: @Contextual UUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: Date? = null,
): BasePlaylist()

@Serializable
data class PlaylistEntry(
    val id: @Contextual UUID,
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