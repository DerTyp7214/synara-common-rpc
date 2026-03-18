package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class MirrorProgress(
    val currentTask: String,
    val processedItems: Int,
    val totalItems: Int,
    val isFinished: Boolean = false,
    val error: String? = null,
    val currentItem: String? = null,
    val currentItemProgress: Float? = null,
    val speed: String? = null
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
    val quality: Int = -1
)
