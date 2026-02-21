package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.PaginatedResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IAlbumService {
    suspend fun byId(id: PlatformUUID): Album?
    suspend fun byIds(ids: List<PlatformUUID>): List<Album>
    suspend fun versions(id: PlatformUUID): List<Album>
    suspend fun byName(page: Int, pageSize: Int, name: String): PaginatedResponse<Album>
    suspend fun rankedSearch(page: Int, pageSize: Int, query: String): PaginatedResponse<Album>
    suspend fun allAlbums(page: Int, pageSize: Int): PaginatedResponse<Album>
    suspend fun deleteAlbums(ids: List<PlatformUUID>): Boolean

    suspend fun byArtist(
        page: Int,
        pageSize: Int,
        artistId: PlatformUUID,
        singles: Boolean = false
    ): PaginatedResponse<Album>
}