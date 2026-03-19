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
    val eta: String? = null,
    val statusMessage: String? = null,
    val syncBreakdown: SyncBreakdown? = null
)

@Serializable
data class SyncBreakdown(
    val songs: Int = 0,
    val artists: Int = 0,
    val albums: Int = 0,
    val images: Int = 0,
    val playlists: Int = 0,
    val userPlaylists: Int = 0
)

@Serializable
data class ProxyInstanceInfo(val id: String, val name: String?)

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
    val likedByUserIds: List<PlatformUUID>? = null,
    val useProxy: Boolean = false,
    val proxyInstanceId: String? = null,
    val targetUserId: PlatformUUID? = null
)
