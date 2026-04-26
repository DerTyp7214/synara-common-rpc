@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.Artist
import dev.dertyp.data.ArtistAlias
import dev.dertyp.data.ArtistSplitAlias
import dev.dertyp.data.Image
import dev.dertyp.data.Playlist
import dev.dertyp.data.RemoteServerPaths
import dev.dertyp.data.Song
import dev.dertyp.data.User
import dev.dertyp.data.UserPlaylist
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A single item in a raw image binary stream.")
data class ImageStreamItem(
    @FieldDoc("The image unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The raw image binary data.")
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageStreamItem) return false
        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@Rpc
@RpcDoc("Exposes local data for server-to-server mirroring.")
interface IMirrorService {
    @RpcDoc("Expose the local file system paths where media files are stored.", errors = ["IllegalStateException"])
    suspend fun getServerPaths(): RemoteServerPaths
    @RpcDoc("Stream all local songs with metadata for mirroring.", errors = ["IllegalStateException"])
    fun getSongs(): Flow<Song>
    @RpcDoc("Stream all local artists for mirroring.", errors = ["IllegalStateException"])
    fun getArtists(): Flow<Artist>
    @RpcDoc("Stream all artist name aliases for mirroring.", errors = ["IllegalStateException"])
    fun getArtistAliases(): Flow<ArtistAlias>
    @RpcDoc("Stream all artist split-name mappings for mirroring.", errors = ["IllegalStateException"])
    fun getArtistSplitAliases(): Flow<ArtistSplitAlias>
    @RpcDoc("Stream all local albums for mirroring.", errors = ["IllegalStateException"])
    fun getAlbums(): Flow<Album>
    @RpcDoc("Stream all local system playlists for mirroring.", errors = ["IllegalStateException"])
    fun getPlaylists(): Flow<Playlist>
    @RpcDoc("Stream all local user playlists for mirroring.", errors = ["IllegalStateException"])
    fun getUserPlaylists(): Flow<UserPlaylist>
    @RpcDoc("Stream all image metadata for mirroring.", errors = ["IllegalStateException"])
    fun getImageMetadata(): Flow<Image>
    @RpcDoc("Stream raw audio data for a song.", errors = ["IllegalStateException"])
    fun getSongData(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("Target audio quality level.") quality: Int = 0,
        @RpcParamDoc("Number of bytes per chunk in the stream.") chunkSize: Int = 4096,
        @RpcParamDoc("Whether to force re-transcoding and duration check.") force: Boolean = true
    ): Flow<ByteArray>
    @RpcDoc("Stream all local user accounts (profiles) for mirroring.", errors = ["IllegalStateException"])
    fun getUsers(): Flow<User>
    @RpcDoc("Stream all songs belonging to a specific system playlist for mirroring.", errors = ["IllegalStateException"])
    fun getSongsByPlaylist(@RpcParamDoc("The playlist unique identifier.") playlistId: PlatformUUID): Flow<Song>
    @RpcDoc("Stream all songs belonging to a specific user playlist for mirroring.", errors = ["IllegalStateException"])
    fun getSongsByUserPlaylist(@RpcParamDoc("The playlist unique identifier.") playlistId: PlatformUUID): Flow<Song>
    @RpcDoc("Stream all songs liked by a specific user for mirroring.", errors = ["IllegalStateException"])
    fun getLikedSongs(@RpcParamDoc("The user unique identifier.") userId: PlatformUUID): Flow<Song>
}
