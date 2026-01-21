package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import dev.dertyp.serializers.UUIDByteListSerializer
import dev.dertyp.serializers.UUIDByteSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed class BasePlaylist {
    @Serializable(with = UUIDByteSerializer::class)
    abstract val id: UUID
    abstract val name: String
    @Serializable(with = UUIDByteListSerializer::class)
    abstract val songs: List<UUID>
    abstract val totalDuration: Long
    @Serializable(with = UUIDByteSerializer::class)
    abstract val imageId: UUID?
}

@Serializable
data class Playlist(
    @Serializable(with = UUIDByteSerializer::class)
    override val id: UUID,
    override val name: String,
    @Serializable(with = UUIDByteListSerializer::class)
    override val songs: List<UUID>,
    override val totalDuration: Long = -1L,
    @Serializable(with = UUIDByteSerializer::class)
    override val imageId: UUID? = null,
): BasePlaylist()

@Serializable
data class UserPlaylist(
    @Serializable(with = UUIDByteSerializer::class)
    override val id: UUID,
    override val name: String,
    @Serializable(with = UUIDByteListSerializer::class)
    override val songs: List<UUID>,
    override val totalDuration: Long = -1L,
    @Serializable(with = UUIDByteSerializer::class)
    override val imageId: UUID? = null,
    @Serializable(with = UUIDByteSerializer::class)
    val creator: UUID,
    val description: String,
    val origin: String? = null,
    @Serializable(with = DateSerializer::class)
    val modifiedAt: Date? = null,
): BasePlaylist()

@Serializable
data class PlaylistEntry(
    @Serializable(with = UUIDByteSerializer::class)
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