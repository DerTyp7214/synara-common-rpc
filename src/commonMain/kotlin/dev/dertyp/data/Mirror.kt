@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Real-time progress information for an active server mirroring task.")
data class MirrorProgress(
    @FieldDoc("The current step of the synchronization process.")
    val currentTask: String,
    @FieldDoc("Number of items processed in the current task.")
    val processedItems: Int,
    @FieldDoc("Total items expected for the current task.")
    val totalItems: Int,
    @FieldDoc("Whether the entire mirroring task has completed.")
    val isFinished: Boolean = false,
    @FieldDoc("Error message if the task failed.")
    val error: String? = null,
    @FieldDoc("The name or ID of the item currently being processed.")
    val currentItem: String? = null,
    @FieldDoc("Fraction (0 to 1) of progress for the current item.")
    val currentItemProgress: Float? = null,
    @FieldDoc("Current transfer speed formatted as a string.")
    val speed: String? = null,
    @FieldDoc("Estimated time of completion for the task.")
    val eta: String? = null,
    @FieldDoc("Human-readable status or log message.")
    val statusMessage: String? = null,
    @FieldDoc("Breakdown of imported and existing items.")
    val syncBreakdown: SyncBreakdown? = null
)

@Serializable
@ModelDoc("Breakdown of newly imported versus existing items in a synchronization.")
data class SyncBreakdown(
    @FieldDoc("New songs imported.") val songs: Int = 0,
    @FieldDoc("Songs that already existed.") val existingSongs: Int = 0,
    @FieldDoc("New artists imported.") val artists: Int = 0,
    @FieldDoc("Artists that already existed.") val existingArtists: Int = 0,
    @FieldDoc("New albums imported.") val albums: Int = 0,
    @FieldDoc("Albums that already existed.") val existingAlbums: Int = 0,
    @FieldDoc("New images imported.") val images: Int = 0,
    @FieldDoc("Images that already existed.") val existingImages: Int = 0,
    @FieldDoc("New system playlists imported.") val playlists: Int = 0,
    @FieldDoc("System playlists that already existed.") val existingPlaylists: Int = 0,
    @FieldDoc("New user playlists imported.") val userPlaylists: Int = 0,
    @FieldDoc("User playlists that already existed.") val existingUserPlaylists: Int = 0,
    @FieldDoc("Total number of errors encountered.") val errors: Int = 0,
    @FieldDoc("Collection of identifiers for items that failed to import.") val failedItems: List<String> = emptyList()
)

@Serializable
@ModelDoc("Information about a proxy instance connected to a Synara server.")
data class ProxyInstanceInfo(
    @FieldDoc("The unique identifier of the proxy instance.")
    val id: String,
    @FieldDoc("The human-readable name of the proxy instance.")
    val name: String?
)

@Serializable
@ModelDoc("Collection of local file system paths for media storage on the server.")
data class RemoteServerPaths(
    @FieldDoc("Path to the main tracks storage directory.") val tracksPath: String?,
    @FieldDoc("Path to the main albums storage directory.") val albumsPath: String?,
    @FieldDoc("Path to the main playlists storage directory.") val playlistsPath: String?,
    @FieldDoc("Path to the custom uploaded audio storage directory.") val customAudioPath: String?,
    @FieldDoc("Collection of alternative paths for track storage.") val secondaryTracksPaths: List<String> = emptyList()
)

@Serializable
@ModelDoc("Configuration for connecting and mirroring data from another Synara server.")
data class RemoteServerConfig(
    @FieldDoc("The host address of the remote server.") val host: String,
    @FieldDoc("The port number of the remote server.") val port: Int,
    @FieldDoc("Username for remote authentication.") val username: String,
    @FieldDoc("Password for remote authentication.") val password: String,
    @FieldDoc("Whether to use SSL/TLS for the connection.") val secure: Boolean = false,
    @FieldDoc("Requested audio quality level for mirrored tracks.") val quality: Int = -1,
    @FieldDoc("Optional collection of system playlist IDs to mirror.") val playlistIds: List<PlatformUUID>? = null,
    @FieldDoc("Optional collection of user playlist IDs to mirror.") val userPlaylistIds: List<PlatformUUID>? = null,
    @FieldDoc("Optional collection of user IDs whose liked songs should be mirrored.") val likedByUserIds: List<PlatformUUID>? = null,
    @FieldDoc("Whether to connect to the remote server via a proxy.") val useProxy: Boolean = false,
    @FieldDoc("The identifier of the proxy instance to use.") val proxyInstanceId: String? = null,
    @FieldDoc("The target user ID on the local server for imported user data.") val targetUserId: PlatformUUID? = null,
    @FieldDoc("Whether this mirroring task is considered a one-time import.") val isImport: Boolean = false
)
