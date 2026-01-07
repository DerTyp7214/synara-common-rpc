package dev.dertyp.services

import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.Playlist
import dev.dertyp.data.PlaylistEntry
import kotlinx.rpc.annotations.Rpc
import java.util.*

@Rpc
interface IPlaylistService {
    suspend fun byId(id: UUID): Playlist?
    suspend fun byIdFull(id: UUID): Pair<String, List<PlaylistEntry>>?
    suspend fun byName(name: String): Playlist?
    suspend fun rankedSearch(page: Int, pageSize: Int, query: String): PaginatedResponse<Playlist>
    suspend fun allPlaylists(page: Int, pageSize: Int): PaginatedResponse<Playlist>
    suspend fun delete(id: UUID): Boolean
}