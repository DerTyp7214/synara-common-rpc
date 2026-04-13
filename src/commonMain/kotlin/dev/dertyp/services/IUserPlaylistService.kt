package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.InsertablePlaylist
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.User
import dev.dertyp.data.UserPlaylist
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Management of personal (user-created) playlists.")
interface IUserPlaylistService {
    @RpcDoc("Get user playlist by ID.")
    suspend fun byId(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): UserPlaylist?
    @RpcDoc("Get multiple user playlists by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of playlist IDs.") ids: List<PlatformUUID>): List<UserPlaylist>
    @RpcDoc("Search user playlists.")
    suspend fun rankedSearch(
        @RpcParamDoc("Optional creator ID to filter by.") creator: PlatformUUID?,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<UserPlaylist>
    @RpcDoc("Get all user playlists.")
    suspend fun allPlaylists(
        @RpcParamDoc("Optional creator ID to filter by.") creator: PlatformUUID?,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<UserPlaylist>
    @RpcDoc("Delete a user playlist.")
    suspend fun delete(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): Boolean
    @RpcDoc("Create a new user playlist or retrieve an existing one by a custom identifier.")
    suspend fun getOrAddPlaylist(
        @RpcParamDoc("The user who owns the playlist.") user: User,
        @RpcParamDoc("Optional unique string identifier from an external source.") customIdentifier: String?,
        @RpcParamDoc("The initial playlist data.") playlist: InsertablePlaylist
    ): PlatformUUID
    @RpcDoc("Add songs to a user playlist.")
    suspend fun addToPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of song IDs and their added timestamps.") songIds: List<Pair<Long, PlatformUUID>>
    ): List<PlatformUUID>
    @RpcDoc("Remove songs from a user playlist.")
    suspend fun removeFromPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of song IDs to remove.") songIds: List<PlatformUUID>
    ): Int
    @RpcDoc("Set the cover image for a user playlist.")
    suspend fun setPlaylistImage(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The image unique identifier.") imageId: PlatformUUID?
    ): Boolean
}
