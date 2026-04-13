package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manages artist data and complex library maintenance.")
interface IArtistService {
    @RpcDoc("Get artist by ID.")
    suspend fun byId(@RpcParamDoc("The artist unique identifier.") id: PlatformUUID): Artist?
    @RpcDoc("Get multiple artists by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of artist IDs.") ids: List<PlatformUUID>): List<Artist>
    @RpcDoc("Ranked artist search.")
    suspend fun rankedSearch(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<Artist>
    @RpcDoc("Set sub-artists for a group.")
    suspend fun setGroup(
        @RpcParamDoc("The group artist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Optional collection of sub-artist IDs.") artistIds: List<PlatformUUID>?
    ): Artist?
    @RpcDoc("List artists in a group.")
    suspend fun byGroup(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The group artist unique identifier.") groupId: PlatformUUID
    ): PaginatedResponse<Artist>
    @RpcDoc("Merge multiple artist records into one.")
    suspend fun mergeArtists(@RpcParamDoc("Configuration for merging artists.") mergeArtists: MergeArtists): Artist?
    @RpcDoc("Split an artist record into multiple artists.")
    suspend fun splitArtist(@RpcParamDoc("Configuration for splitting an artist.") splitArtist: SplitArtist): List<Artist>
    @RpcDoc("Get all artists in the library.")
    suspend fun allArtists(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<Artist>
    @RpcDoc("Manually create an artist record.")
    suspend fun createArtist(
        @RpcParamDoc("Name of the artist.") name: String,
        @RpcParamDoc("Whether the artist is a group.") isGroup: Boolean = false,
        @RpcParamDoc("Optional biography or description.") about: String = "",
        @RpcParamDoc("Optional MusicBrainz ID.") musicBrainzId: PlatformUUID? = null
    ): Artist
    @RpcDoc("Search for an artist directly on MusicBrainz.")
    suspend fun searchArtistOnMusicBrainz(
        @RpcParamDoc("The search query.") query: String,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<MusicBrainzArtist>
    @RpcDoc("Fetch and link MusicBrainz ID for an artist.")
    suspend fun fetchMusicBrainzId(@RpcParamDoc("The artist unique identifier.") id: PlatformUUID): Artist?
    @RpcDoc("Link an artist record to a MusicBrainz ID.")
    suspend fun setMusicBrainzId(
        @RpcParamDoc("The artist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The MusicBrainz ID to link.") musicBrainzId: PlatformUUID?
    ): Artist?
    @RpcDoc("Stream all artists that are missing a MusicBrainz ID.")
    fun artistsWithoutMusicBrainzIdFlow(): Flow<Artist>
    @RpcDoc("Stream IDs of all artists that are missing a MusicBrainz ID.")
    fun artistIdsWithoutMusicBrainzId(): Flow<PlatformUUID>
}