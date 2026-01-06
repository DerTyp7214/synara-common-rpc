package dev.dertyp.services

import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.UserSong
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.rpc.annotations.Rpc
import java.time.Instant
import java.util.*

@Rpc
interface ISongService {
    suspend fun setLiked(id: UUID, userId: UUID, liked: Boolean, addedAt: Instant? = null): UserSong?
    suspend fun byId(id: UUID, userId: UUID): UserSong?
    suspend fun byIds(ids: Collection<UUID>, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byTitle(page: Int, pageSize: Int, title: String, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byArtist(page: Int, pageSize: Int, artistId: UUID, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byAlbum(page: Int, pageSize: Int, albumId: UUID, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byPlaylist(page: Int, pageSize: Int, playlistId: UUID, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byUserPlaylist(page: Int, pageSize: Int, playlistId: UUID, userId: UUID): PaginatedResponse<UserSong>
    suspend fun byTidalTrackIds(ids: Collection<String>, userId: UUID): List<UserSong>
    suspend fun byTidalTracks(tracks: Collection<IMetadataService.Track>, userId: UUID): List<UserSong>
    suspend fun likedSongs(page: Int, pageSize: Int, explicit: Boolean, userId: UUID): PaginatedResponse<UserSong>
    suspend fun allSongs(page: Int, pageSize: Int, explicit: Boolean, userId: UUID): PaginatedResponse<UserSong>

    suspend fun deleteSongs(ids: Collection<UUID>): Boolean

    suspend fun rankedSearch(
        page: Int,
        pageSize: Int,
        query: String,
        explicit: Boolean,
        userId: UUID,
        liked: Boolean = false
    ): PaginatedResponse<UserSong>
}