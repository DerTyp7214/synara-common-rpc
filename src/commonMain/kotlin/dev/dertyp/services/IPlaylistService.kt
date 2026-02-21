package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.Playlist
import dev.dertyp.data.PlaylistEntry
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IPlaylistService {
    suspend fun byId(id: PlatformUUID): Playlist?
    suspend fun byIds(ids: List<PlatformUUID>): List<Playlist>
    suspend fun byIdFull(id: PlatformUUID): Pair<String, List<PlaylistEntry>>?
    suspend fun byName(name: String): Playlist?
    suspend fun rankedSearch(page: Int, pageSize: Int, query: String): PaginatedResponse<Playlist>
    suspend fun allPlaylists(page: Int, pageSize: Int): PaginatedResponse<Playlist>
    suspend fun delete(id: PlatformUUID): Boolean
}