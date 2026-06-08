@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Manages artist data and complex library maintenance.")
interface IArtistService {
    @RpcDoc("Get artist by ID.")
    suspend fun byId(@RpcParamDoc("The artist unique identifier.") id: PlatformUUID): Artist?
    @RpcDoc("Get artist by MusicBrainz ID.")
    suspend fun byMusicBrainzId(@RpcParamDoc("The MusicBrainz artist UUID.") mbId: PlatformUUID): List<Artist>
    @RpcDoc("Get multiple artists by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of artist IDs.") ids: List<PlatformUUID>): List<Artist>
    @RpcDoc("Ranked artist search.")
    suspend fun rankedSearch(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<Artist>
    @RequiresCapability(UserCapability.EDIT)
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
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Merge multiple artist records into one.")
    suspend fun mergeArtists(@RpcParamDoc("Configuration for merging artists.") mergeArtists: MergeArtists): Artist?
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Split an artist record into multiple artists.")
    suspend fun splitArtist(@RpcParamDoc("Configuration for splitting an artist.") splitArtist: SplitArtist): List<Artist>
    @RpcDoc("Get all artists in the library.")
    suspend fun allArtists(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<Artist>

    @RpcDoc("Search for artists by color.")
    suspend fun byColor(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The target color in ARGB format.") color: Int,
        @RpcParamDoc("The allowed range (0-255).") range: Int = 20
    ): PaginatedResponse<Artist>

    @RequiresCapability(UserCapability.EDIT)
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
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Fetch and link MusicBrainz ID for an artist.")
    suspend fun fetchMusicBrainzId(@RpcParamDoc("The artist unique identifier.") id: PlatformUUID): Artist?
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Link an artist record to a MusicBrainz ID.")
    suspend fun setMusicBrainzId(
        @RpcParamDoc("The artist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The MusicBrainz ID to link.") musicBrainzId: PlatformUUID?
    ): Artist?
    @RpcDoc("Search for artist images using a metadata provider.")
    suspend fun searchArtistImages(
        @RpcParamDoc("The metadata provider to use.") type: IMetadataService.MetadataType,
        @RpcParamDoc("The artist name to search for.") query: String,
        @RpcParamDoc("Maximum number of images to return.") limit: Int = 50
    ): List<IMetadataService.Image>
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Set an artist's image from a direct URL.")
    suspend fun setArtistImageByUrl(
        @RpcParamDoc("The artist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The direct image URL.") url: String
    ): Artist?
    @RpcDoc("Stream all artists that are missing a MusicBrainz ID.")
    fun artistsWithoutMusicBrainzIdFlow(): Flow<Artist>
    @RpcDoc("Stream IDs of all artists that are missing a MusicBrainz ID.")
    fun artistIdsWithoutMusicBrainzId(): Flow<PlatformUUID>

    @RpcDoc("List aliases for an artist.")
    suspend fun aliases(@RpcParamDoc("The artist unique identifier.") id: PlatformUUID): List<ArtistAlias>

    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Add an alias to an artist.")
    suspend fun addAlias(
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("The alternative name.") name: String
    ): Boolean

    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Remove an alias from an artist.")
    suspend fun removeAlias(
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("The alternative name.") name: String
    ): Boolean
}
