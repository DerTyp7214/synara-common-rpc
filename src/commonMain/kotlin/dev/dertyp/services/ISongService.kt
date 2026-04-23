package dev.dertyp.services

import dev.dertyp.PlatformInstant
import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.SongTag
import dev.dertyp.data.UserSong
import dev.dertyp.rpc.annotations.RestFileResponse
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("The primary interface for song discovery, streaming, and metadata.")
interface ISongService {
    @RpcDoc("Toggle favorite status.", adminOnly = false)
    suspend fun setLiked(
        @RpcParamDoc("The unique UUID of the song.") id: PlatformUUID,
        @RpcParamDoc("Whether to mark as liked.") liked: Boolean,
        @RpcParamDoc("Optional timestamp of when it was added.") addedAt: PlatformInstant? = null
    ): UserSong?
    @RpcDoc("Manually set song lyrics.")
    suspend fun setLyrics(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("List of lyric lines.") lyrics: List<String>
    ): UserSong?
    @RpcDoc("Update song artists.")
    suspend fun setArtists(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of artist IDs.") artistIds: List<PlatformUUID>
    ): UserSong?
    @RpcDoc("Link a song to its MusicBrainz Recording record.")
    suspend fun setMusicBrainzId(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The MusicBrainz Recording UUID.") musicBrainzId: PlatformUUID?
    ): UserSong?
    @RpcDoc("Trigger automatic MusicBrainz ID matching for a song.")
    suspend fun fetchMusicBrainzId(@RpcParamDoc("The song unique identifier.") id: PlatformUUID): UserSong?
    @RpcDoc("Get song by its unique identifier.")
    suspend fun byId(@RpcParamDoc("The song unique identifier.") id: PlatformUUID): UserSong?
    @RpcDoc("Find songs by their MusicBrainz Recording ID.")
    suspend fun byMusicBrainzId(@RpcParamDoc("The MusicBrainz Recording UUID.") musicBrainzId: PlatformUUID): List<UserSong>
    @RpcDoc("Get multiple songs by their unique identifiers.")
    suspend fun byIds(@RpcParamDoc("Collection of song IDs.") ids: Collection<PlatformUUID>): List<UserSong>
    @RpcDoc("Search for songs by title.")
    suspend fun byTitle(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The song title to search for.") title: String
    ): PaginatedResponse<UserSong>
    @RpcDoc("List songs by a specific artist.")
    suspend fun byArtist(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID
    ): PaginatedResponse<UserSong>
    @RpcDoc("List songs liked by the user for a specific artist.")
    suspend fun likedByArtist(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("Whether to include explicit content.") explicit: Boolean
    ): PaginatedResponse<UserSong>
    @RpcDoc("List songs in an album.")
    suspend fun byAlbum(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The album unique identifier.") albumId: PlatformUUID
    ): PaginatedResponse<UserSong>
    @RpcDoc("List songs in a system playlist.")
    suspend fun byPlaylist(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The playlist unique identifier.") playlistId: PlatformUUID
    ): PaginatedResponse<UserSong>
    @RpcDoc("List songs in a user playlist.")
    suspend fun byUserPlaylist(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The user playlist unique identifier.") playlistId: PlatformUUID
    ): PaginatedResponse<UserSong>
    @RpcDoc("Find songs by their original platform-specific unique identifiers.")
    suspend fun byOriginalIds(@RpcParamDoc("Collection of original platform-specific track identifiers.") ids: Collection<String>): List<UserSong>
    @RpcDoc("Find songs matching external metadata records.")
    suspend fun byOriginalTracks(@RpcParamDoc("Collection of track metadata.") tracks: Collection<IMetadataService.Track>): List<UserSong>
    @RpcDoc("Get all songs liked by the current user.")
    suspend fun likedSongs(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("Whether to include explicit content.") explicit: Boolean
    ): PaginatedResponse<UserSong>
    @RpcDoc("Get all songs with optional filtering.")
    suspend fun allSongs(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("Whether to include explicit content.") explicit: Boolean,
        @RpcParamDoc("Filter by specific tags.") tags: List<SongTag> = emptyList(),
        @RpcParamDoc("Invert the tag filter.") invertTags: Boolean = false,
    ): PaginatedResponse<UserSong>

    @RpcDoc("Delete multiple songs from the library.")
    suspend fun deleteSongs(@RpcParamDoc("Collection of song IDs to delete.") ids: Collection<PlatformUUID>): Boolean

    @RpcDoc("Perform a ranked search for songs.")
    suspend fun rankedSearch(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String,
        @RpcParamDoc("Whether to include explicit content.") explicit: Boolean,
        @RpcParamDoc("Only search within liked songs.") liked: Boolean = false
    ): PaginatedResponse<UserSong>

    @RpcDoc("Stream song audio data for playback.")
    @RestFileResponse
    fun streamSong(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Byte offset to start streaming from.") offset: Long = 0,
        @RpcParamDoc("Size of each data chunk.") chunkSize: Int = 4096
    ): Flow<ByteArray>?
    @RpcDoc("Download song audio in specific quality.", errors = ["IOException", "IllegalStateException"])
    @RestFileResponse
    fun downloadSong(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Target audio quality.") quality: Int,
        @RpcParamDoc("Byte offset to start from.") offset: Long = 0,
        @RpcParamDoc("Size of each data chunk.") chunkSize: Int = 4096
    ): Flow<ByteArray>?
    @RpcDoc("Get the total size of the song's audio stream.")
    suspend fun getStreamSize(@RpcParamDoc("The song unique identifier.") id: PlatformUUID): Long
    @RpcDoc("Get the size of the song audio for a specific quality.")
    suspend fun getDownloadSize(
        @RpcParamDoc("The song unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The requested quality.") quality: Int
    ): Long

    @RpcDoc("Stream all song IDs with optional filtering.")
    fun allSongIds(
        @RpcParamDoc("Whether to include explicit content.") explicit: Boolean,
        @RpcParamDoc("Filter by specific tags.") tags: List<SongTag> = emptyList(),
        @RpcParamDoc("Invert the tag filter.") invertTags: Boolean = false
    ): Flow<PlatformUUID>
    @RpcDoc("Stream all IDs of songs liked by the current user.")
    fun likedSongIds(@RpcParamDoc("Whether to include explicit content.") explicit: Boolean): Flow<PlatformUUID>
    @RestGet
    @RpcDoc("Stream song IDs belonging to an artist.")
    fun songIdsByArtist(@RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID): Flow<PlatformUUID>
    @RestGet
    @RpcDoc("Stream song IDs belonging to an album.")
    fun songIdsByAlbum(@RpcParamDoc("The album unique identifier.") albumId: PlatformUUID): Flow<PlatformUUID>
    @RestGet
    @RpcDoc("Stream song IDs belonging to a system playlist.")
    fun songIdsByPlaylist(@RpcParamDoc("The playlist unique identifier.") playlistId: PlatformUUID): Flow<PlatformUUID>
    @RestGet
    @RpcDoc("Stream song IDs belonging to a user playlist.")
    fun songIdsByUserPlaylist(@RpcParamDoc("The user playlist unique identifier.") playlistId: PlatformUUID): Flow<PlatformUUID>
}
