package dev.dertyp.services

import dev.dertyp.data.InsertablePlaylist
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.User
import dev.dertyp.data.UserPlaylist
import kotlinx.rpc.annotations.Rpc
import java.util.UUID

@Rpc
interface IUserPlaylistService {
    suspend fun byId(id: UUID): UserPlaylist?
    suspend fun byIds(ids: List<UUID>): List<UserPlaylist>
    suspend fun rankedSearch(creator: UUID?, page: Int, pageSize: Int, query: String): PaginatedResponse<UserPlaylist>
    suspend fun allPlaylists(creator: UUID?, page: Int, pageSize: Int): PaginatedResponse<UserPlaylist>
    suspend fun delete(id: UUID): Boolean
    suspend fun getOrAddPlaylist(user: User, customIdentifier: String?, playlist: InsertablePlaylist): UUID
    suspend fun addToPlaylist(id: UUID, songIds: List<Pair<Long, UUID>>): List<UUID>
    suspend fun removeFromPlaylist(id: UUID, songIds: List<UUID>): Int
    suspend fun setPlaylistImage(id: UUID, imageId: UUID?): Boolean
}
