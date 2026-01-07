package dev.dertyp.services

import dev.dertyp.data.Album
import dev.dertyp.data.PaginatedResponse
import kotlinx.rpc.annotations.Rpc
import java.util.*

@Rpc
interface IAlbumService {
    suspend fun byId(id: UUID): Album?
    suspend fun byName(page: Int, pageSize: Int, name: String): PaginatedResponse<Album>
    suspend fun rankedSearch(page: Int, pageSize: Int, query: String): PaginatedResponse<Album>
    suspend fun allAlbums(page: Int, pageSize: Int): PaginatedResponse<Album>
    suspend fun deleteAlbums(ids: List<UUID>): Boolean

    suspend fun byArtist(
        page: Int,
        pageSize: Int,
        artistId: UUID,
        singles: Boolean = false
    ): PaginatedResponse<Album>
}