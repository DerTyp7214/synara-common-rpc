package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Album
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.UserSong
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Service for content discovery and song recommendations.")
interface IDiscoveryService {
    @RpcDoc("Create a mosaic of songs matching the colors of an image.")
    suspend fun createSongMosaic(
        @RpcParamDoc("The source image data.") image: ByteArray,
        @RpcParamDoc("Target grid width.") width: Int,
        @RpcParamDoc("Target grid height.") height: Int,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("Search range for colors (0-255).") range: Int = 20
    ): PaginatedResponse<UserSong>

    @RpcDoc("Create a mosaic of albums matching the colors of an image.")
    suspend fun createAlbumMosaic(
        @RpcParamDoc("The source image data.") image: ByteArray,
        @RpcParamDoc("Target grid width.") width: Int,
        @RpcParamDoc("Target grid height.") height: Int,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("Search range for colors (0-255).") range: Int = 20
    ): PaginatedResponse<Album>

    @RpcDoc("Get songs similar to the provided seed tracks.")
    suspend fun getSimilarSongs(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs similar to the songs in the provided user playlist.")
    suspend fun getSimilarSongsByPlaylist(
        @RpcParamDoc("The user playlist unique identifier to use as a seed.") playlistId: PlatformUUID,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs with similar BPM to the provided seed tracks.")
    suspend fun getSimilarSongsByBpm(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs with similar energy to the provided seed tracks.")
    suspend fun getSimilarSongsByEnergy(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs with similar mood (valence) to the provided seed tracks.")
    suspend fun getSimilarSongsByMood(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs by the same composers as the provided seed tracks.")
    suspend fun getSongsBySameComposers(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs by the same lyricists as the provided seed tracks.")
    suspend fun getSongsBySameLyricists(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("Get songs by the same producers as the provided seed tracks.")
    suspend fun getSongsBySameProducers(
        @RpcParamDoc("List of song unique identifiers to use as a seed.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>
}
