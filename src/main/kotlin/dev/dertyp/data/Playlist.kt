package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed class BasePlaylist {
    @Contextual
    abstract val id: UUID
    abstract val name: String
    @Contextual
    abstract val songs: List<UUID>
    abstract val totalDuration: Long
    @Contextual
    abstract val imageId: UUID?
}

@Serializable
data class Playlist(
    @Contextual
    override val id: UUID,
    override val name: String,
    override val songs: List<@Contextual UUID>,
    override val totalDuration: Long = -1L,
    @Contextual
    override val imageId: UUID? = null,
): BasePlaylist()

@Serializable
data class UserPlaylist(
    @Contextual
    override val id: UUID,
    override val name: String,
    override val songs: List<@Contextual UUID>,
    override val totalDuration: Long = -1L,
    @Contextual
    override val imageId: UUID? = null,
    @Contextual
    val creator: UUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: Date? = null,
): BasePlaylist()

@Serializable
data class PlaylistEntry(
    @Contextual
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