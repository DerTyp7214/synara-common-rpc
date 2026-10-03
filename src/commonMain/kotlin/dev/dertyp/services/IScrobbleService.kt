package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ListenedAlbum
import dev.dertyp.data.ListenedArtist
import dev.dertyp.data.PlaybackReport
import dev.dertyp.data.RecentListens
import dev.dertyp.data.ScrobbleRequest
import dev.dertyp.rpc.annotations.REMOVED_IN_API_9
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Submit playback scrobbles and observe the current user's recently listened songs, artists and albums.")
interface IScrobbleService {
    @RestPost
    @RpcDoc("Report the library song the current user is now playing. Auto-clears after the song's duration unless reported again.")
    suspend fun nowPlaying(
        @RpcParamDoc("The library song now playing.") songId: PlatformUUID
    )

    @RestPost
    @RpcDoc("Report playback progress for the current user: call on play, pause, resume, seek and every 10-15 seconds while playing. Returns the server's epoch milliseconds at receipt so clients can align their clocks.")
    suspend fun reportPlayback(
        @RpcParamDoc("The current playback state.") report: PlaybackReport
    ): Long

    @RestPost
    @RpcDoc("Clear the current user's now-playing state, e.g. when playback stops.")
    suspend fun clearNowPlaying()

    @RestPost
    @RpcDoc("Record that the current user finished listening to a library song.")
    suspend fun listened(
        @RpcParamDoc("The completed listen.") request: ScrobbleRequest
    )

    @RestGet
    @RpcDoc("Get the current user's recently listened songs and current now-playing once.")
    suspend fun recentListens(
        @RpcParamDoc("Maximum number of recent songs to return. Clamped to 1..1000.") limit: Int
    ): RecentListens

    @Deprecated(REMOVED_IN_API_9 + " Use IChangeService.observeChanges and recentListens.")
    @RestGet
    @RpcDoc("Stream the current user's recently listened songs and current now-playing, re-emitting on changes (debounced 100ms). @IChangeService.observeChanges reports the LISTENS topic of @ChangeTopic instead, after which @IScrobbleService.recentListens reads them once.")
    fun recentListensFlow(
        @RpcParamDoc("Maximum number of recent songs to return. Clamped to 1..1000.") limit: Int
    ): Flow<RecentListens>

    @RestGet
    @RpcDoc("Get the current user's recently listened artists, most recently played first.")
    suspend fun recentArtists(
        @RpcParamDoc("Maximum number of artists to return. Clamped to 1..1000.") limit: Int
    ): List<ListenedArtist>

    @Deprecated(REMOVED_IN_API_9 + " Use IChangeService.observeChanges and recentArtists.")
    @RestGet
    @RpcDoc("Stream the current user's recently listened artists, most recently played first, re-emitting on changes (debounced 100ms). @IChangeService.observeChanges reports the LISTENS topic of @ChangeTopic instead, after which @IScrobbleService.recentArtists reads them once.")
    fun recentArtistsFlow(
        @RpcParamDoc("Maximum number of artists to return. Clamped to 1..1000.") limit: Int
    ): Flow<List<ListenedArtist>>

    @RestGet
    @RpcDoc("Get the current user's recently listened albums, most recently played first.")
    suspend fun recentAlbums(
        @RpcParamDoc("Maximum number of albums to return. Clamped to 1..1000.") limit: Int
    ): List<ListenedAlbum>

    @Deprecated(REMOVED_IN_API_9 + " Use IChangeService.observeChanges and recentAlbums.")
    @RestGet
    @RpcDoc("Stream the current user's recently listened albums, most recently played first, re-emitting on changes (debounced 100ms). @IChangeService.observeChanges reports the LISTENS topic of @ChangeTopic instead, after which @IScrobbleService.recentAlbums reads them once.")
    fun recentAlbumsFlow(
        @RpcParamDoc("Maximum number of albums to return. Clamped to 1..1000.") limit: Int
    ): Flow<List<ListenedAlbum>>
}
