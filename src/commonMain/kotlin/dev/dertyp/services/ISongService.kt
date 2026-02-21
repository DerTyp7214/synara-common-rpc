package dev.dertyp.services

import dev.dertyp.PlatformInstant
import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.UserSong
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface ISongService {
    suspend fun setLiked(id: PlatformUUID, liked: Boolean, addedAt: PlatformInstant? = null): UserSong?
    suspend fun setLyrics(id: PlatformUUID, lyrics: List<String>): UserSong?
    suspend fun byId(id: PlatformUUID): UserSong?
    suspend fun byIds(ids: Collection<PlatformUUID>): PaginatedResponse<UserSong>
    suspend fun byTitle(page: Int, pageSize: Int, title: String): PaginatedResponse<UserSong>
    suspend fun byArtist(page: Int, pageSize: Int, artistId: PlatformUUID): PaginatedResponse<UserSong>
    suspend fun byAlbum(page: Int, pageSize: Int, albumId: PlatformUUID): PaginatedResponse<UserSong>
    suspend fun byPlaylist(page: Int, pageSize: Int, playlistId: PlatformUUID): PaginatedResponse<UserSong>
    suspend fun byUserPlaylist(page: Int, pageSize: Int, playlistId: PlatformUUID): PaginatedResponse<UserSong>
    suspend fun byTidalTrackIds(ids: Collection<String>): List<UserSong>
    suspend fun byTidalTracks(tracks: Collection<IMetadataService.Track>): List<UserSong>
    suspend fun likedSongs(page: Int, pageSize: Int, explicit: Boolean): PaginatedResponse<UserSong>
    suspend fun allSongs(page: Int, pageSize: Int, explicit: Boolean): PaginatedResponse<UserSong>

    suspend fun deleteSongs(ids: Collection<PlatformUUID>): Boolean

    suspend fun rankedSearch(
        page: Int,
        pageSize: Int,
        query: String,
        explicit: Boolean,
        liked: Boolean = false
    ): PaginatedResponse<UserSong>

    fun streamSong(id: PlatformUUID, offset: Long = 0): Flow<ByteArray>?
    suspend fun getStreamSize(id: PlatformUUID): Long

    fun allSongIds(explicit: Boolean): Flow<PlatformUUID>
    fun likedSongIds(explicit: Boolean): Flow<PlatformUUID>
    fun songIdsByArtist(artistId: PlatformUUID): Flow<PlatformUUID>
    fun songIdsByAlbum(albumId: PlatformUUID): Flow<PlatformUUID>
    fun songIdsByPlaylist(playlistId: PlatformUUID): Flow<PlatformUUID>
    fun songIdsByUserPlaylist(playlistId: PlatformUUID): Flow<PlatformUUID>
}
