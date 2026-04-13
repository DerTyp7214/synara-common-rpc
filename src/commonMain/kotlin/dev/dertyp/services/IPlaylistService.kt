package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.Playlist
import dev.dertyp.data.PlaylistEntry
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Management of system playlists.")
interface IPlaylistService {
    @RpcDoc("Get system playlist by ID.")
    suspend fun byId(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): Playlist?
    @RpcDoc("Get multiple system playlists by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of playlist IDs.") ids: List<PlatformUUID>): List<Playlist>
    @RpcDoc("Get system playlist with all track entries.")
    suspend fun byIdFull(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): Pair<String, List<PlaylistEntry>>?
    @RpcDoc("Get system playlist by name.")
    suspend fun byName(@RpcParamDoc("The name of the playlist.") name: String): Playlist?
    @RpcDoc("Search system playlists.")
    suspend fun rankedSearch(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<Playlist>
    @RpcDoc("Get all system playlists.")
    suspend fun allPlaylists(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<Playlist>
    @RpcDoc("Delete a system playlist.")
    suspend fun delete(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): Boolean
}