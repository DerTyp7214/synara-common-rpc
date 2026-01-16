package dev.dertyp.services

import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.UserSong
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import java.time.Instant
import java.util.*

@Rpc
interface ISongService {
    suspend fun setLiked(id: UUID, liked: Boolean, addedAt: Instant? = null): UserSong?
    suspend fun setLyrics(id: UUID, lyrics: List<String>): UserSong?
    suspend fun byId(id: UUID): UserSong?
    suspend fun byIds(ids: Collection<UUID>): PaginatedResponse<UserSong>
    suspend fun byTitle(page: Int, pageSize: Int, title: String): PaginatedResponse<UserSong>
    suspend fun byArtist(page: Int, pageSize: Int, artistId: UUID): PaginatedResponse<UserSong>
    suspend fun byAlbum(page: Int, pageSize: Int, albumId: UUID): PaginatedResponse<UserSong>
    suspend fun byPlaylist(page: Int, pageSize: Int, playlistId: UUID): PaginatedResponse<UserSong>
    suspend fun byUserPlaylist(page: Int, pageSize: Int, playlistId: UUID): PaginatedResponse<UserSong>
    suspend fun byTidalTrackIds(ids: Collection<String>): List<UserSong>
    suspend fun byTidalTracks(tracks: Collection<IMetadataService.Track>): List<UserSong>
    suspend fun likedSongs(page: Int, pageSize: Int, explicit: Boolean): PaginatedResponse<UserSong>
    suspend fun allSongs(page: Int, pageSize: Int, explicit: Boolean): PaginatedResponse<UserSong>

    suspend fun deleteSongs(ids: Collection<UUID>): Boolean

    suspend fun rankedSearch(
        page: Int,
        pageSize: Int,
        query: String,
        explicit: Boolean,
        liked: Boolean = false
    ): PaginatedResponse<UserSong>

    fun streamSong(id: UUID, offset: Long = 0): Flow<ByteArray>?
    suspend fun getStreamSize(id: UUID): Long
}