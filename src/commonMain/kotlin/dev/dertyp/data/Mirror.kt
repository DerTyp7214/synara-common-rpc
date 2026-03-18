@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class MirrorProgress(
    val currentTask: String,
    val processedItems: Int,
    val totalItems: Int,
    val isFinished: Boolean = false,
    val error: String? = null,
    val currentItem: String? = null,
    val currentItemProgress: Float? = null,
    val speed: String? = null,
    val eta: String? = null
)

@Serializable
data class RemoteServerPaths(
    val tracksPath: String?,
    val albumsPath: String?,
    val playlistsPath: String?,
    val customAudioPath: String?,
    val secondaryTracksPaths: List<String> = emptyList()
)

@Serializable
data class RemoteServerConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val secure: Boolean = false,
    val quality: Int = -1,
    val playlistIds: List<PlatformUUID>? = null,
    val userPlaylistIds: List<PlatformUUID>? = null,
    val likedByUserIds: List<PlatformUUID>? = null
)
