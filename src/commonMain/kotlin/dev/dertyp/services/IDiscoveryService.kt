package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.UserSong
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Service for content discovery and song recommendations.")
interface IDiscoveryService {
    @RpcDoc("Get songs similar to the provided seed tracks.")
    suspend fun getSimilarSongs(
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
