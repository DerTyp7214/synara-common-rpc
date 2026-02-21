package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.InsertablePlaylist
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.User
import dev.dertyp.data.UserPlaylist
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IUserPlaylistService {
    suspend fun byId(id: PlatformUUID): UserPlaylist?
    suspend fun byIds(ids: List<PlatformUUID>): List<UserPlaylist>
    suspend fun rankedSearch(creator: PlatformUUID?, page: Int, pageSize: Int, query: String): PaginatedResponse<UserPlaylist>
    suspend fun allPlaylists(creator: PlatformUUID?, page: Int, pageSize: Int): PaginatedResponse<UserPlaylist>
    suspend fun delete(id: PlatformUUID): Boolean
    suspend fun getOrAddPlaylist(user: User, customIdentifier: String?, playlist: InsertablePlaylist): PlatformUUID
    suspend fun addToPlaylist(id: PlatformUUID, songIds: List<Pair<Long, PlatformUUID>>): List<PlatformUUID>
    suspend fun removeFromPlaylist(id: PlatformUUID, songIds: List<PlatformUUID>): Int
    suspend fun setPlaylistImage(id: PlatformUUID, imageId: PlatformUUID?): Boolean
}
