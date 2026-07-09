package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.RecentListens
import dev.dertyp.data.ScrobbleRequest
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Submit playback scrobbles and observe the current user's recently listened songs.")
interface IScrobbleService {
    @RestPost
    @RpcDoc("Report the library song the current user is now playing. Auto-clears after the song's duration unless reported again.")
    suspend fun nowPlaying(
        @RpcParamDoc("The library song now playing.") songId: PlatformUUID
    )

    @RestPost
    @RpcDoc("Clear the current user's now-playing state, e.g. when playback stops.")
    suspend fun clearNowPlaying()

    @RestPost
    @RpcDoc("Record that the current user finished listening to a library song.")
    suspend fun listened(
        @RpcParamDoc("The completed listen.") request: ScrobbleRequest
    )

    @RestGet
    @RpcDoc("Stream the current user's recently listened songs and current now-playing, re-emitting on changes (debounced 100ms).")
    fun recentListensFlow(
        @RpcParamDoc("Maximum number of recent songs to return; clamped to 1..1000.") limit: Int
    ): Flow<RecentListens>
}
