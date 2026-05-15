@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Manages albums and their metadata.")
interface IAlbumService {
    @RpcDoc("Get album by ID.")
    suspend fun byId(@RpcParamDoc("The album unique identifier.") id: PlatformUUID): Album?
    @RpcDoc("Get album by MusicBrainz ID.")
    suspend fun byMusicBrainzId(@RpcParamDoc("The MusicBrainz release (or release-group) UUID.") mbId: PlatformUUID): List<Album>
    @RpcDoc("Get multiple albums by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of album IDs.") ids: List<PlatformUUID>): List<Album>
    @RpcDoc("List different versions of an album.")
    suspend fun versions(@RpcParamDoc("The album unique identifier.") id: PlatformUUID): List<Album>
    @RpcDoc("Search albums by name.")
    suspend fun byName(
        @RpcParamDoc("Page index (starting from 0).") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The album name to search for.") name: String
    ): PaginatedResponse<Album>
    @RpcDoc("Ranked album search.")
    suspend fun rankedSearch(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<Album>
    @RpcDoc("Get all albums in the library.")
    suspend fun allAlbums(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<Album>
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Update album metadata.")
    suspend fun updateAlbum(@RpcParamDoc("The album object with updated fields.") album: Album): Album?
    @RequiresCapability(UserCapability.DELETE)
    @RpcDoc("Delete multiple albums from the library.")
    suspend fun deleteAlbums(@RpcParamDoc("Collection of album IDs to delete.") ids: List<PlatformUUID>): Boolean
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Fetch and link MusicBrainz ID for an album.")
    suspend fun fetchMusicBrainzId(@RpcParamDoc("The album unique identifier.") id: PlatformUUID): Album?
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Manually set MusicBrainz ID for an album.")
    suspend fun setMusicBrainzId(
        @RpcParamDoc("The album unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The MusicBrainz Release UUID.") musicBrainzId: PlatformUUID?
    ): Album?

    @RpcDoc("List albums by artist.")
    suspend fun byArtist(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("Whether to include singles.") singles: Boolean = false
    ): PaginatedResponse<Album>
}